package com.onlystudents.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.note.client.SubscriptionFeignClient;
import com.onlystudents.note.dto.CreateNoteRequest;
import com.onlystudents.note.dto.NoteDTO;
import com.onlystudents.note.dto.UpdateNoteRequest;
import com.onlystudents.note.entity.Note;
import com.onlystudents.note.mapper.NoteMapper;
import com.onlystudents.note.service.NoteSearchService;
import com.onlystudents.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    
    private final NoteMapper noteMapper;
    private final NoteSearchService noteSearchService;
    private final SubscriptionFeignClient subscriptionFeignClient;
    private final RabbitTemplate rabbitTemplate;


    @Override
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
        
        return convertToDTO(note);
    }
    
    @Override
    public void deleteNote(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            return;
        }
        
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        note.setStatus(4); // 已删除
        noteMapper.updateById(note);
    }
    
    @Override
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
        if (note.getStatus() != 2) { // 未发布
            if (!note.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
            }
        }
        
        if (note.getVisibility() > 0 && !note.getUserId().equals(userId)) {
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
    public List<NoteDTO> getHotNotes(Integer limit) {
        if (limit == null || limit > 100) {
            limit = 20;
        }
        List<Note> notes = noteMapper.selectHotNotes(limit);
        return notes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<NoteDTO> getLatestNotes(Integer limit) {
        if (limit == null || limit > 100) {
            limit = 20;
        }
        List<Note> notes = noteMapper.selectLatestNotes(limit);
        return notes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<NoteDTO> getUserNotes(Long userId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        wrapper.ne(Note::getStatus, 4); // 排除已删除
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
        
        // 同步到Elasticsearch
        noteSearchService.updateNote(note);
        // 异步发送到MQ，由NoteSyncListener处理ES同步
        try {
            rabbitTemplate.convertAndSend("note.exchange", "note.sync", note);
            log.info("笔记 [{}] 已发布，同步消息已发送到MQ", noteId);
        } catch (Exception e) {
            log.error("发送笔记同步消息失败: noteId={}", noteId, e);
            // 可以选择抛异常回滚，或者继续执行（最终一致性）
            // throw new BusinessException(ResultCode.SYSTEM_ERROR, "同步失败");
        }
    }
    
    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        BeanUtils.copyProperties(note, dto);
        return dto;
    }
}
