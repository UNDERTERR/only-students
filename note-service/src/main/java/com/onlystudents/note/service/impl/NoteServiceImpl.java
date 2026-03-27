package com.onlystudents.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.common.result.Result;
import com.onlystudents.note.client.FileFeignClient;
import com.onlystudents.note.client.NotificationFeignClient;
import com.onlystudents.note.client.OperationLogFeignClient;
import com.onlystudents.note.client.SubscriptionFeignClient;
import com.onlystudents.note.client.UserFeignClient;
import com.onlystudents.note.dto.CreateNoteRequest;
import com.onlystudents.note.dto.NoteDTO;
import com.onlystudents.note.dto.NoteStatsDTO;
import com.onlystudents.note.dto.UpdateNoteRequest;
import com.onlystudents.note.entity.Note;
import com.onlystudents.common.event.note.NotePublishEvent;
import com.onlystudents.note.mapper.NoteMapper;
import com.onlystudents.note.service.NoteService;
import com.onlystudents.note.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final FileFeignClient fileFeignClient;
    private final UserFeignClient userFeignClient;
    private final NotificationFeignClient notificationFeignClient;
    private final OperationLogFeignClient operationLogFeignClient;
    private final RabbitTemplate rabbitTemplate;
    private final TagService tagService;


    @Override
    @Caching(evict = {
            @CacheEvict(value = "hotNotes", allEntries = true),
            @CacheEvict(value = "latestNotes", allEntries = true)
    })
    public NoteDTO createNote(CreateNoteRequest request, Long userId) {
        // 检查用户是否可以发布内容
        Result<Boolean> canPostResult = userFeignClient.canUserPost(userId);
        if (canPostResult == null || canPostResult.getData() == null || !canPostResult.getData()) {
            throw new BusinessException(400, "您已被封禁，无法发布笔记");
        }
        
        Note note = new Note();
        BeanUtils.copyProperties(request, note);
        note.setUserId(userId);
        note.setStatus(0); // 草稿状态
        note.setViewCount(0);
        note.setFavoriteCount(0);
        note.setCommentCount(0);
        note.setShareCount(0);
        note.setHotScore(0.0);

        // 获取用户信息，设置 educationLevel, schoolId, schoolName
        try {
            // 先尝试通过 Feign 获取
            Result<Map<String, Object>> userResult = userFeignClient.getUserById(userId);
            log.info("Feign获取用户信息结果: userId={}, result={}", userId, userResult);
            
            Map<String, Object> userData = null;
            if (userResult != null && userResult.getData() != null) {
                userData = userResult.getData();
            }
            
            if (userData != null) {
                log.info("用户数据所有key: {}", userData.keySet());
                log.info("用户数据: userId={}, schoolId={}, schoolName={}, educationLevel={}", 
                    userId, userData.get("schoolId"), userData.get("schoolName"), userData.get("educationLevel"));
                if (userData.get("educationLevel") != null) {
                    note.setEducationLevel(toInt(userData.get("educationLevel")));
                }
                if (userData.get("schoolId") != null) {
                    note.setSchoolId(toLong(userData.get("schoolId")));
                }
                if (userData.get("schoolName") != null) {
                    note.setSchoolName((String) userData.get("schoolName"));
                }
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}", userId, e);
        }

        noteMapper.insert(note);
        log.info("笔记创建成功: noteId={}, schoolId={}, schoolName={}", note.getId(), note.getSchoolId(), note.getSchoolName());

        // 保存标签关联
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tagService.setNoteTags(note.getId(), request.getTags());
        }

        return convertToDTO(note);
    }

    @Override
    @CacheEvict(value = "noteDetail", key = "#noteId")
    public NoteDTO updateNote(Long noteId, UpdateNoteRequest request, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        BeanUtils.copyProperties(request, note);
        
        // 保存后状态重置为草稿，需要重新发布才能再次审核
        note.setStatus(0);
        noteMapper.updateById(note);

        // 更新标签关联
        if (request.getTags() != null) {
            tagService.setNoteTags(noteId, request.getTags());
        }

        return convertToDTO(note);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "noteDetail", key = "#noteId"),
        @CacheEvict(value = "latestNotes", allEntries = true)
    })
    public void deleteNote(Long noteId, Long userId) {
        log.info("开始删除笔记: noteId={}, userId={}", noteId, userId);
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在: noteId={}", noteId);
            return;
        }

        if (!note.getUserId().equals(userId)) {
            log.warn("无权限删除笔记: noteId={}, userId={}, noteUserId={}", noteId, userId, note.getUserId());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        log.info("删除笔记: noteId={}, title={}", noteId, note.getTitle());

        // 删除附件文件
        deleteAttachments(note.getAttachments(), userId);

        // 删除标签关联
        tagService.deleteNoteTags(noteId);

        // 减少学校笔记数
        if (note.getSchoolId() != null) {
            try {
                userFeignClient.decrementSchoolNotes(note.getSchoolId());
            } catch (Exception e) {
                log.warn("减少学校笔记数失败: schoolId={}", note.getSchoolId(), e);
            }
        }

        // 使用 MyBatis Plus 逻辑删除，会自动设置 deleted=1
        noteMapper.deleteById(noteId);
        log.info("笔记已从数据库删除: noteId={}", noteId);

        // 发送MQ消息通知删除ES文档
        try {
            rabbitTemplate.convertAndSend("note.exchange", "note.delete", noteId);
            rabbitTemplate.convertAndSend("note.exchange", "note.vector.delete", noteId);
            log.info("笔记 [{}] 已删除，删除消息已发送到MQ", noteId);
        } catch (Exception e) {
            log.error("发送笔记删除消息失败: noteId={}", noteId, e);
        }
        
        log.info("笔记删除完成: noteId={}", noteId);
    }

    /**
     * 删除附件文件
     */
    private void deleteAttachments(String attachmentsJson, Long userId) {
        if (attachmentsJson == null || attachmentsJson.isEmpty()) {
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<java.util.Map<String, Object>> attachments = objectMapper.readValue(attachmentsJson,
                    new com.fasterxml.jackson.core.type.TypeReference<List<java.util.Map<String, Object>>>() {
                    });

            for (java.util.Map<String, Object> att : attachments) {
                Long fileId = Long.valueOf(att.get("fileId").toString());
                try {
                    fileFeignClient.deleteFile(fileId, userId);
                    log.info("删除文件成功: fileId={}", fileId);
                } catch (Exception e) {
                    log.error("删除文件失败: fileId={}", fileId, e);
                }
            }
        } catch (Exception e) {
            log.error("解析附件列表失败: {}", attachmentsJson, e);
        }
    }

    @Override
    @Cacheable(value = "noteDetail", key = "#noteId", unless = "#result == null")
    public NoteDTO getNoteById(Long noteId) {
        if (noteId == null) {
            return null;
        }

        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 2) {
            return null;
        }
        return convertToDTO(note);
    }

    @Override
    public NoteDTO getNoteDetail(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }
        // visibility: 0-公开, 1-仅订阅可见, 2-仅付费可见, 3-订阅后付费可见, 4-仅自己可见
        if (note.getStatus() != 2) { // 未发布
            if (!note.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
            }
        }
        if (note.getVisibility() == 4 && !note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }
        incrementViewCount(noteId);
        return convertToDTO(note);
    }

    @Override
    @Cacheable(value = "hotNotes", key = "'hot_' + #p0 + '_' + #p1", unless = "#result == null || #result.isEmpty()")
    public List<NoteDTO> getHotNotes(Integer limit, Long currentUserId) {
        if (limit == null || limit > 100) {
            limit = 20;
        }

        // 查询公开的笔记
        List<Note> publicNotes = noteMapper.selectHotNotes(limit);

        // 如果用户已登录，还需要查询该用户的所有已发布笔记（无论可见性）
        if (currentUserId != null) {
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getUserId, currentUserId);
            wrapper.eq(Note::getDeleted, 0);
            wrapper.eq(Note::getStatus, 2); // 已发布
            wrapper.orderByDesc(Note::getHotScore);
            wrapper.last("LIMIT " + limit);
            List<Note> userNotes = noteMapper.selectList(wrapper);

            // 合并并去重（用户的笔记优先）
            publicNotes.removeIf(note -> userNotes.stream().anyMatch(u -> u.getId().equals(note.getId())));
            publicNotes.addAll(0, userNotes);
        }

        return publicNotes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "latestNotes", key = "'latest_' + #p0 + '_' + #p1", unless = "#result == null || #result.isEmpty()")
    public List<NoteDTO> getLatestNotes(Integer limit, Long currentUserId) {
        if (limit == null || limit > 100) {
            limit = 20;
        }
        log.info("获取首页最新数据，当前用户: {}, limit: {}", currentUserId, limit);

        // 查询公开的笔记
        List<Note> publicNotes = noteMapper.selectLatestNotes(limit);

        // 如果用户已登录，还需要查询该用户的所有已发布笔记（无论可见性）
        if (currentUserId != null) {
            List<Note> userNotes = noteMapper.selectUserLatestNotes(currentUserId, limit);

            // 合并并去重（用户的笔记优先）
            publicNotes.removeIf(note -> userNotes.stream().anyMatch(u -> u.getId().equals(note.getId())));
            publicNotes.addAll(0, userNotes);
        }

        // 批量查询标签
        List<Long> noteIds = publicNotes.stream().map(Note::getId).collect(Collectors.toList());
        Map<Long, List<String>> tagsMap = tagService.getNoteTagsBatch(noteIds);

        // 转换DTO
        return publicNotes.stream().map(note -> {
            NoteDTO dto = convertToDTOWithoutRemote(note);
            dto.setTags(tagsMap.get(note.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<NoteDTO> getNotesBySchoolId(Long schoolId, Integer limit) {
        if (limit == null || limit > 100) {
            limit = 20;
        }
        log.info("获取学校笔记: schoolId={}, limit={}", schoolId, limit);
        
        List<Note> notes = noteMapper.selectNotesBySchoolId(schoolId, limit);
        
        // 批量查询标签
        List<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toList());
        Map<Long, List<String>> tagsMap = tagService.getNoteTagsBatch(noteIds);

        return notes.stream().map(note -> {
            NoteDTO dto = convertToDTOWithoutRemote(note);
            dto.setTags(tagsMap.get(note.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<NoteDTO> getSubscribedNotes(List<Long> creatorIds, Integer page, Integer limit) {
        if (creatorIds == null || creatorIds.isEmpty()) {
            return new ArrayList<>();
        }
        if (limit == null || limit > 100) {
            limit = 20;
        }
        if (page == null || page < 1) {
            page = 1;
        }
        
        int offset = (page - 1) * limit;
        log.info("获取订阅笔记: creatorIds={}, page={}, limit={}", creatorIds, page, limit);
        
        List<Note> notes = noteMapper.selectNotesByUserIds(creatorIds, offset, limit);
        
        // 批量查询标签
        List<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toList());
        Map<Long, List<String>> tagsMap = tagService.getNoteTagsBatch(noteIds);

        return notes.stream().map(note -> {
            NoteDTO dto = convertToDTOWithoutRemote(note);
            dto.setTags(tagsMap.get(note.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 转换DTO但不调用远程服务（用于批量处理）
     */
    private NoteDTO convertToDTOWithoutRemote(Note note) {
        NoteDTO dto = new NoteDTO();
        BeanUtils.copyProperties(note, dto);
        
        // 如果 authorAvatar 为空，使用默认值
        if (dto.getAuthorNickname() == null || dto.getAuthorNickname().isEmpty()) {
            dto.setAuthorNickname("用户_" + note.getUserId());
        }
        
        return dto;
    }

    @Override
    public List<NoteDTO> getUserNotes(Long userId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        wrapper.orderByDesc(Note::getCreatedAt);

        List<Note> notes = noteMapper.selectList(wrapper);
        return notes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void incrementViewCount(Long noteId) {
        noteMapper.incrementViewCount(noteId);
    }

    @Override
    public void publishNote(Long noteId, Long userId) {
        // 检查用户是否可以发布内容
        Result<Boolean> canPostResult = userFeignClient.canUserPost(userId);
        if (canPostResult == null || canPostResult.getData() == null || !canPostResult.getData()) {
            throw new BusinessException(400, "您已被封禁，无法发布笔记");
        }
        
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 发布时设置为待审核状态，需要管理员审核后才能显示
        note.setStatus(1); // 待审核
        noteMapper.updateById(note);
        
        log.info("笔记提交审核: noteId={}, userId={}", noteId, userId);

        // 注意：暂时不增加学校笔记数，等审核通过后再增加
        // 注意：暂时不发送同步消息，等审核通过后再发送
    }

    @Override
    public List<Long> getNoteIdsByUserId(Long userId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        wrapper.select(Note::getId);
        List<Note> notes = noteMapper.selectList(wrapper);
        return notes.stream().map(Note::getId).collect(Collectors.toList());
    }

    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        BeanUtils.copyProperties(note, dto);

        // 如果 authorAvatar 为空，尝试通过 userId 获取用户信息
        if (dto.getAuthorAvatar() == null || dto.getAuthorAvatar().isEmpty()) {
            try {
                Result<Map<String, Object>> userResult = userFeignClient.getUserById(note.getUserId());
                if (userResult != null && userResult.getData() != null) {
                    Map<String, Object> userData = userResult.getData();
                    dto.setAuthorNickname((String) userData.get("nickname"));
                    dto.setAuthorAvatar((String) userData.get("avatar"));
                }
            } catch (Exception e) {
                log.error("获取用户信息失败: userId={}", note.getUserId(), e);
            }
        }

        // 加载标签
        List<String> tags = tagService.getNoteTags(note.getId());
        dto.setTags(tags);

        return dto;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    @Override
    public java.util.Map<String, Object> getCreatorNoteStats(Long creatorId) {
        java.util.Map<String, Object> stats = noteMapper.selectCreatorNoteStats(creatorId);
        if (stats == null) {
            stats = new java.util.HashMap<>();
        }
        return stats;
    }

    @Override
    public NoteStatsDTO getNoteStats() {
        NoteStatsDTO stats = new NoteStatsDTO();
        stats.setTotalNotes(noteMapper.countTotalNotes());
        stats.setTodayNewNotes(noteMapper.countTodayNewNotes());
        stats.setWeekNewNotes(noteMapper.countWeekNewNotes());
        stats.setMonthNewNotes(noteMapper.countMonthNewNotes());
        stats.setPublishedNotes(noteMapper.countPublishedNotes());
        stats.setPendingAuditNotes(noteMapper.countPendingAuditNotes());
        stats.setRejectedNotes(noteMapper.countRejectedNotes());
        return stats;
    }

    @Override
    public List<NoteDTO> getPendingAuditNotes(Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        List<Note> notes = noteMapper.selectPendingAuditNotes(offset, size);
        return notes.stream().map(this::toNoteDTO).collect(Collectors.toList());
    }

    @Override
    public Long getPendingAuditCount() {
        return noteMapper.countPendingAuditNotes();
    }

    @Override
    public void auditPass(Long noteId, Long adminId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "笔记不存在");
        }
        if (note.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "笔记不是待审核状态");
        }
        note.setStatus(2); // 审核通过，设置为已发布
        note.setPublishTime(LocalDateTime.now());
        noteMapper.updateById(note);
        
        log.info("笔记审核通过: noteId={}, adminId={}", noteId, adminId);
        
        // 审核通过后，增加学校笔记数
        if (note.getSchoolId() != null) {
            try {
                userFeignClient.incrementSchoolNotes(note.getSchoolId());
                log.info("审核通过增加学校笔记数: schoolId={}", note.getSchoolId());
            } catch (Exception e) {
                log.error("增加学校笔记数失败: schoolId={}", note.getSchoolId(), e);
            }
        }
        
        // 审核通过后，发送同步消息
        try {
            rabbitTemplate.convertAndSend("note.exchange", "note.sync", note);
            rabbitTemplate.convertAndSend("note.exchange", "note.vector.sync", note);
            log.info("审核通过，笔记同步消息已发送到MQ: noteId={}", noteId);
        } catch (Exception e) {
            log.error("发送消息失败: noteId={}", noteId, e);
        }
        
        // 发送审核通过通知给用户
        try {
            String title = "笔记审核通过";
            String content = "您的笔记《" + note.getTitle() + "》已审核通过，快去分享给小伙伴吧！";
            notificationFeignClient.sendNotification(note.getUserId(), 1, title, content);
            log.info("发送审核通过通知: noteId={}, userId={}", noteId, note.getUserId());
        } catch (Exception e) {
            log.error("发送审核通过通知失败: noteId={}", noteId, e);
        }
        
        // 记录操作日志
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("adminId", adminId);
            logMap.put("operationType", "AUDIT_PASS");
            logMap.put("operationDesc", "审核通过笔记: " + note.getTitle());
            logMap.put("requestMethod", "POST");
            logMap.put("requestUrl", "/note/audit/" + noteId + "/pass");
            logMap.put("status", 1);
            operationLogFeignClient.saveLog(logMap);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    @Override
    public void auditReject(Long noteId, String reason, Long adminId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "笔记不存在");
        }
        if (note.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "笔记不是待审核状态");
        }
        String noteTitle = note.getTitle();
        note.setStatus(3);
        noteMapper.updateById(note);
        log.info("笔记审核拒绝: noteId={}, reason={}, adminId={}", noteId, reason, adminId);
        
        // 发送审核拒绝通知给用户
        try {
            String title = "笔记审核未通过";
            String content = "您的笔记《" + noteTitle + "》审核未通过，原因：" + reason;
            notificationFeignClient.sendNotification(note.getUserId(), 1, title, content);
            log.info("发送审核拒绝通知: noteId={}, userId={}", noteId, note.getUserId());
        } catch (Exception e) {
            log.error("发送审核拒绝通知失败: noteId={}", noteId, e);
        }
        
        // 记录操作日志
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("adminId", adminId);
            logMap.put("operationType", "AUDIT_REJECT");
            logMap.put("operationDesc", "审核拒绝笔记: " + noteTitle + ", 原因: " + reason);
            logMap.put("requestMethod", "POST");
            logMap.put("requestUrl", "/note/audit/" + noteId + "/reject");
            logMap.put("status", 1);
            operationLogFeignClient.saveLog(logMap);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    @Override
    public void setNoteToDraft(Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "笔记不存在");
        }
        note.setStatus(0);
        noteMapper.updateById(note);
        log.info("设置笔记为草稿状态: noteId={}", noteId);
    }

    @Override
    public Map<String, Object> getNoteListForAdmin(Integer page, Integer size, Integer status, String keyword) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Note::getId);
        
        if (status != null) {
            wrapper.eq(Note::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Note::getTitle, keyword);
        }
        
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        
        List<Note> notes = noteMapper.selectList(wrapper);
        List<NoteDTO> list = notes.stream().map(this::toNoteDTO).collect(Collectors.toList());
        
        // Get total count
        LambdaQueryWrapper<Note> countWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            countWrapper.eq(Note::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            countWrapper.like(Note::getTitle, keyword);
        }
        Long total = noteMapper.selectCount(countWrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public void deleteNoteByAdmin(Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "笔记不存在");
        }
        noteMapper.deleteById(noteId);
        log.info("管理员删除笔记: noteId={}", noteId);
        
        // 记录操作日志
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("adminId", 1L);
            logMap.put("operationType", "DELETE_NOTE");
            logMap.put("operationDesc", "删除笔记: " + note.getTitle());
            logMap.put("requestMethod", "DELETE");
            logMap.put("requestUrl", "/note/admin/" + noteId);
            logMap.put("status", 1);
            operationLogFeignClient.saveLog(logMap);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    @Override
    public void incrementViewCountByAdmin(Long noteId, Integer count) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "笔记不存在");
        }
        if (count == null || count <= 0) {
            count = 1;
        }
        note.setViewCount(note.getViewCount() + count);
        noteMapper.updateById(note);
        log.info("管理员增加浏览量: noteId={}, count={}", noteId, count);
        
        // 记录操作日志
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("adminId", 1L);
            logMap.put("operationType", "INCREMENT_VIEW_COUNT");
            logMap.put("operationDesc", "增加浏览量: 笔记《" + note.getTitle() + "》增加 " + count);
            logMap.put("requestMethod", "POST");
            logMap.put("requestUrl", "/note/admin/" + noteId + "/view-count");
            logMap.put("status", 1);
            operationLogFeignClient.saveLog(logMap);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    private NoteDTO toNoteDTO(Note note) {
        if (note == null) {
            return null;
        }
        NoteDTO dto = new NoteDTO();
        BeanUtils.copyProperties(note, dto);
        return dto;
    }
}
