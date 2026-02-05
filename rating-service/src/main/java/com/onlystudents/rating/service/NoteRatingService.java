package com.onlystudents.rating.service;

import com.onlystudents.common.result.Result;
import com.onlystudents.rating.dto.NoteRatingDTO;

import java.util.List;

/**
 * 笔记评分服务
 */
public interface NoteRatingService {
    
    /**
     * 评分笔记
     */
    Result<Void> rateNote(Long noteId, Long userId, Integer score);
    
    /**
     * 获取笔记平均评分
     */
    Result<Double> getAverageRating(Long noteId);
    
    /**
     * 获取笔记评分人数
     */
    Result<Long> getRatingCount(Long noteId);
    
    /**
     * 获取用户评分
     */
    Result<NoteRatingDTO> getUserRating(Long noteId, Long userId);
    
    /**
     * 获取用户评分列表
     */
    Result<List<NoteRatingDTO>> getUserRatings(Long userId);
}
