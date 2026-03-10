package com.onlystudents.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.benmanes.caffeine.cache.Cache;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.common.result.Result;
import com.onlystudents.note.client.FileFeignClient;
import com.onlystudents.note.client.SubscriptionFeignClient;
import com.onlystudents.note.client.UserFeignClient;
import com.onlystudents.note.dto.CreateNoteRequest;
import com.onlystudents.note.dto.NoteDTO;
import com.onlystudents.note.dto.UpdateNoteRequest;
import com.onlystudents.note.entity.Note;
import com.onlystudents.common.event.note.NotePublishEvent;
import com.onlystudents.note.mapper.NoteMapper;
import com.onlystudents.note.service.NoteService;
import com.onlystudents.note.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final SubscriptionFeignClient subscriptionFeignClient;
    private final FileFeignClient fileFeignClient;
    private final UserFeignClient userFeignClient;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final TagService tagService;
    private final CacheManager cacheManager;

    private static final String CACHE_KEY_NOTE = "note:detail:";
    private static final String CACHE_KEY_HOT = "hotNotes";
    private static final String CACHE_KEY_LATEST = "latestNotes";
    private static final long CACHE_TTL_MINUTES = 5;

    private Cache<Long, NoteDTO> noteLocalCache;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NoteServiceImpl(NoteMapper noteMapper,
                           SubscriptionFeignClient subscriptionFeignClient,
                           FileFeignClient fileFeignClient,
                           UserFeignClient userFeignClient,
                           RabbitTemplate rabbitTemplate,
                           StringRedisTemplate redisTemplate,
                           TagService tagService,
                           CacheManager cacheManager) {
        this.noteMapper = noteMapper;
        this.subscriptionFeignClient = subscriptionFeignClient;
        this.fileFeignClient = fileFeignClient;
        this.userFeignClient = userFeignClient;
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
        this.tagService = tagService;
        this.cacheManager = cacheManager;
        initLocalCache();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private void initLocalCache() {
        this.noteLocalCache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(1, java.util.concurrent.TimeUnit.MINUTES)
                .build();
    }


    @Override
    @Caching(evict = {
            @CacheEvict(value = "hotNotes", allEntries = true),
            @CacheEvict(value = "latestNotes", allEntries = true)
    })
    public NoteDTO createNote(CreateNoteRequest request, Long userId) {
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
    public NoteDTO updateNote(Long noteId, UpdateNoteRequest request, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        BeanUtils.copyProperties(request, note);
        noteMapper.updateById(note);

        // 更新标签关联
        if (request.getTags() != null) {
            tagService.setNoteTags(noteId, request.getTags());
        }

        clearNoteCache(noteId);

        return convertToDTO(note);
    }

    @Override
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

        // 清除缓存
        clearNoteCache(noteId);

        // 发送MQ消息通知删除ES文档
        try {
            rabbitTemplate.convertAndSend("note.exchange", "note.delete", noteId);
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
    public NoteDTO getNoteById(Long noteId) {
        if (noteId == null) {
            return null;
        }

        NoteDTO cached = noteLocalCache.getIfPresent(noteId);
        if (cached != null) {
            log.debug("L1缓存命中: noteId={}", noteId);
            return cached;
        }

        String redisKey = CACHE_KEY_NOTE + noteId;
        Object redisValue = redisTemplate.opsForValue().get(redisKey);
        if (redisValue != null) {
            log.debug("L2缓存命中: noteId={}", noteId);
            NoteDTO noteDTO = convertRedisValue(redisValue);
            if (noteDTO != null) {
                noteLocalCache.put(noteId, noteDTO);
            }
            return noteDTO;
        }

        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 2) {
            return null;
        }
        NoteDTO noteDTO = convertToDTO(note);

        try {
            String jsonValue = objectMapper.writeValueAsString(noteDTO);
            redisTemplate.opsForValue().set(redisKey, jsonValue, CACHE_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("序列化NoteDTO失败", e);
        }
        noteLocalCache.put(noteId, noteDTO);
        log.debug("数据库查询并写入缓存: noteId={}", noteId);

        return noteDTO;
    }

    private NoteDTO convertRedisValue(Object redisValue) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            if (redisValue instanceof String) {
                return mapper.readValue((String) redisValue, NoteDTO.class);
            }
            return mapper.convertValue(redisValue, NoteDTO.class);
        } catch (Exception e) {
            log.error("Redis值转换失败", e);
            return null;
        }
    }

    private void clearNoteCache(Long noteId) {
        noteLocalCache.invalidate(noteId);
        String redisKey = CACHE_KEY_NOTE + noteId;
        redisTemplate.delete(redisKey);
        log.info("清除笔记缓存: noteId={}", noteId);
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

        // 直接转换，不查询标签（标签按需加载）
        return publicNotes.stream().map(this::convertToDTOWithoutRemote).collect(Collectors.toList());
    }

    @Override
    public List<NoteDTO> getNotesBySchoolId(Long schoolId, Integer limit) {
        if (limit == null || limit > 100) {
            limit = 20;
        }
        log.info("获取学校笔记: schoolId={}, limit={}", schoolId, limit);
        
        List<Note> notes = noteMapper.selectNotesBySchoolId(schoolId, limit);
        
        // 直接转换，不查询标签
        return notes.stream().map(this::convertToDTOWithoutRemote).collect(Collectors.toList());
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
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        note.setStatus(2); // 已发布
        note.setPublishTime(LocalDateTime.now());
        noteMapper.updateById(note);

        // 如果有学校，增加学校笔记数
        if (note.getSchoolId() != null) {
            log.info("准备增加学校笔记数, schoolId={}, noteId={}", note.getSchoolId(), noteId);
            try {
                    Result<Void> result = userFeignClient.incrementSchoolNotes(note.getSchoolId());
                    log.info("Feign增加学校笔记数结果: schoolId={}, result={}", note.getSchoolId(), result);

            } catch (Exception e) {
                log.error("增加学校笔记数失败: schoolId={}", note.getSchoolId(), e);
            }
        } else {
            log.warn("笔记没有学校信息, noteId={}, schoolId={}", noteId, note.getSchoolId());
        }

        clearNoteCache(noteId);

        try {
            // 发送笔记同步消息
            rabbitTemplate.convertAndSend("note.exchange", "note.sync", note);
            log.info("笔记 [{}] 已发布，同步消息已发送到MQ", noteId + ":" + note);

            // 发送笔记发布成功通知给作者
            NotePublishEvent event = new NotePublishEvent(noteId, userId, note.getTitle(), note.getCoverImage());
            rabbitTemplate.convertAndSend("note.exchange", "note.publish", event);
            log.info("笔记发布通知事件已发送: noteId={}", noteId);
        } catch (Exception e) {
            log.error("发送消息失败: noteId={}", noteId, e);
        }
    }

    @Override
    public List<Long> getNoteIdsByUserId(Long userId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        wrapper.select(Note::getId);
        List<Note> notes = noteMapper.selectList(wrapper);
        return notes.stream().map(Note::getId).collect(Collectors.toList());
    }

    /**
     * 清除列表缓存
     */
    private void clearListCache() {
        try {
            redisTemplate.delete(CACHE_KEY_HOT);
            redisTemplate.delete(CACHE_KEY_LATEST);
            log.debug("清除笔记列表缓存");
        } catch (Exception e) {
            log.warn("清除缓存失败", e);
        }
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
}
