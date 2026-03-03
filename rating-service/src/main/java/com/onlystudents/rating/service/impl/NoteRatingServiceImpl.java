package com.onlystudents.rating.service.impl;

import com.onlystudents.common.event.note.NoteRatingEvent;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.rating.dto.NoteRatingDTO;
import com.onlystudents.rating.entity.NoteRating;
import com.onlystudents.rating.mapper.NoteRatingMapper;
import com.onlystudents.rating.service.NoteRatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteRatingServiceImpl implements NoteRatingService {
    
    private final NoteRatingMapper ratingMapper;
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    @Transactional
    public Result<Void> rateNote(Long noteId, Long userId, Integer score) {
        if (score < 1 || score > 5) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评分必须在1-5之间");
        }
        
        // 查询是否已评分
        NoteRating exist = ratingMapper.selectByNoteAndUser(noteId, userId);
        
        if (exist != null) {
            // 更新评分
            exist.setScore(score);
            ratingMapper.updateById(exist);
        } else {
            // 创建评分
            NoteRating rating = new NoteRating();
            rating.setNoteId(noteId);
            rating.setUserId(userId);
            rating.setScore(score);
            ratingMapper.insert(rating);
        }
        
        // 获取统计信息
        Double avgScore = ratingMapper.selectAverageScoreByNoteId(noteId);
        Long count = ratingMapper.countByNoteId(noteId);
        
        // 发送事件
        NoteRatingEvent event = new NoteRatingEvent(noteId, userId, score, avgScore, count);
        rabbitTemplate.convertAndSend("rating.exchange", "rating.updated", event);
        log.info("发送评分事件: noteId={}, userId={}, score={}, avg={}, count={}", 
                noteId, userId, score, avgScore, count);
        
        return Result.success();
    }
    
    @Override
    public Result<Double> getAverageRating(Long noteId) {
        Double avgScore = ratingMapper.selectAverageScoreByNoteId(noteId);
        return Result.success(avgScore != null ? avgScore : 0.0);
    }
    
    @Override
    public Result<Long> getRatingCount(Long noteId) {
        Long count = ratingMapper.countByNoteId(noteId);
        return Result.success(count);
    }
    
    @Override
    public Result<NoteRatingDTO> getUserRating(Long noteId, Long userId) {
        NoteRating rating = ratingMapper.selectByNoteAndUser(noteId, userId);
        if (rating == null) {
            return Result.success(null);
        }
        NoteRatingDTO dto = new NoteRatingDTO();
        BeanUtils.copyProperties(rating, dto);
        return Result.success(dto);
    }
    
    @Override
    public Result<List<NoteRatingDTO>> getUserRatings(Long userId) {
        List<NoteRating> list = ratingMapper.selectListByUser(userId);
        List<NoteRatingDTO> dtoList = list.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    private NoteRatingDTO convertToDTO(NoteRating rating) {
        NoteRatingDTO dto = new NoteRatingDTO();
        BeanUtils.copyProperties(rating, dto);
        return dto;
    }
}
