package com.onlystudents.rating.service;

import com.onlystudents.common.result.Result;
import com.onlystudents.rating.dto.NoteFavoriteDTO;

import java.util.List;

/**
 * 笔记收藏服务
 */
public interface NoteFavoriteService {
    
    /**
     * 收藏笔记
     */
    Result<Void> favoriteNote(Long noteId, Long userId, Long folderId);
    
    /**
     * 取消收藏
     */
    Result<Void> unfavoriteNote(Long noteId, Long userId);
    
    /**
     * 检查是否已收藏
     */
    Result<Boolean> isFavorited(Long noteId, Long userId);
    
    /**
     * 获取笔记收藏数
     */
    Result<Long> getFavoriteCount(Long noteId);
    
    /**
     * 获取用户收藏列表
     */
    Result<List<NoteFavoriteDTO>> getUserFavorites(Long userId);
}
