package com.onlystudents.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.common.utils.JsonSerializerUtils;
import com.onlystudents.note.client.SubscriptionFeignClient;
import com.onlystudents.note.dto.CreateNoteRequest;
import com.onlystudents.note.dto.NoteDTO;
import com.onlystudents.note.dto.UpdateNoteRequest;
import com.onlystudents.note.entity.Note;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final SubscriptionFeignClient subscriptionFeignClient;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final TagService tagService;

    private static final String CACHE_KEY_NOTE = "note:detail:";
    private static final String CACHE_KEY_HOT = "hotNotes";
    private static final String CACHE_KEY_LATEST = "latestNotes";
    private static final long CACHE_TTL_MINUTES = 5;


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
        note.setLikeCount(0);
        note.setFavoriteCount(0);
        note.setCommentCount(0);
        note.setShareCount(0);
        note.setHotScore(0.0);

        noteMapper.insert(note);

        // 保存标签关联
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tagService.setNoteTags(note.getId(), request.getTags());
        }

        return convertToDTO(note);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "notes", key = "#p0"),
        @CacheEvict(value = "hotNotes", allEntries = true),
        @CacheEvict(value = "latestNotes", allEntries = true)
    })
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

        return convertToDTO(note);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "notes", key = "#p0"),
        @CacheEvict(value = "hotNotes", allEntries = true),
        @CacheEvict(value = "latestNotes", allEntries = true)
    })
    public void deleteNote(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            return;
        }

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 删除标签关联
        tagService.deleteNoteTags(noteId);

        // 使用 MyBatis Plus 逻辑删除，会自动设置 deleted=1
        noteMapper.deleteById(noteId);

        // 发送MQ消息通知删除ES文档
        try {
            rabbitTemplate.convertAndSend("note.exchange", "note.delete", noteId);
            log.info("笔记 [{}] 已删除，删除消息已发送到MQ", noteId);
        } catch (Exception e) {
            log.error("发送笔记删除消息失败: noteId={}", noteId, e);
        }
    }

    @Override
    @Cacheable(value = "notes", key = "#p0", unless = "#result == null")
    public NoteDTO getNoteById(Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 2) { // 只返回已发布的
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }
        return convertToDTO(note);
    }

    @Override
    public NoteDTO getNoteDetail(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }

        // 检查可见性
        // visibility: 0-公开, 1-仅订阅可见, 2-仅付费可见, 3-订阅后付费可见, 4-仅自己可见
        if (note.getStatus() != 2) { // 未发布
            if (!note.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
            }
        }

        // 仅自己可见
        if (note.getVisibility() == 4 && !note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }

        // 需要订阅或付费的可见性（1, 2, 3）
        if ((note.getVisibility() == 1 || note.getVisibility() == 2 || note.getVisibility() == 3) 
                && !note.getUserId().equals(userId)) {
            // 通过Feign调用subscription-service检查是否订阅
            try {
                Result<Boolean> result = subscriptionFeignClient.checkSubscription(note.getUserId(), userId);
                if (result.getData() == null || !result.getData()) {
                    throw new BusinessException(ResultCode.SUBSCRIPTION_REQUIRED);
                }
            } catch (Exception e) {
                log.error("检查订阅状态失败: noteId={}, userId={}, creatorId={}", noteId, userId, note.getUserId(), e);
                throw new BusinessException(ResultCode.SUBSCRIPTION_REQUIRED);
            }
        }

        // 增加浏览量
        incrementViewCount(noteId);

        return convertToDTO(note);
    }

    @Override
    @Cacheable(value = "hotNotes", key = "#p0", unless = "#result == null || #result.isEmpty()")
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
    @Cacheable(value = "latestNotes", key = "#p0", unless = "#result == null || #result.isEmpty()")
    public List<NoteDTO> getLatestNotes(Integer limit, Long currentUserId) {
        if (limit == null || limit > 100) {
            limit = 20;
        }
        log.info("获取首页数据，当前用户: {}", currentUserId);
        
        // 查询公开的笔记
        List<Note> publicNotes = noteMapper.selectLatestNotes(limit);
        
        // 如果用户已登录，还需要查询该用户的所有已发布笔记（无论可见性）
        if (currentUserId != null) {
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getUserId, currentUserId);
            wrapper.eq(Note::getStatus, 2); // 已发布
            wrapper.orderByDesc(Note::getPublishTime);
            wrapper.last("LIMIT " + limit);
            List<Note> userNotes = noteMapper.selectList(wrapper);
            
            // 合并并去重（用户的笔记优先）
            publicNotes.removeIf(note -> userNotes.stream().anyMatch(u -> u.getId().equals(note.getId())));
            publicNotes.addAll(0, userNotes);
        }
        
        return publicNotes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<NoteDTO> getUserNotes(Long userId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        // @TableLogic 会自动过滤 deleted=1 的记录
        wrapper.orderByDesc(Note::getCreatedAt);

        List<Note> notes = noteMapper.selectList(wrapper);
        return notes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void incrementViewCount(Long noteId) {
        noteMapper.incrementViewCount(noteId);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "notes", key = "#p0"),
        @CacheEvict(value = "hotNotes", allEntries = true),
        @CacheEvict(value = "latestNotes", allEntries = true)
    })
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

        try {

            rabbitTemplate.convertAndSend("note.exchange", "note.sync", note);
            log.info("笔记 [{}] 已发布，同步消息已发送到MQ", noteId+":"+note);
        } catch (Exception e) {
            log.error("发送笔记同步消息失败: noteId={}", noteId, e);
            // 可以选择抛异常回滚，或者继续执行（最终一致性）
            // throw new BusinessException(ResultCode.SYSTEM_ERROR, "同步消息发送失败");
        }
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
        
        // 加载标签
        List<String> tags = tagService.getNoteTags(note.getId());
        dto.setTags(tags);
        
        return dto;
    }
}
