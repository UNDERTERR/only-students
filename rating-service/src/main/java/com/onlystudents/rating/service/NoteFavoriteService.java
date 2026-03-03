package com.onlystudents.rating.service;

import com.onlystudents.common.result.Result;
import com.onlystudents.rating.dto.CreateFolderRequest;
import com.onlystudents.rating.dto.FavoriteFolderDTO;
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
    
    /**
     * 获取用户收藏列表（按收藏夹筛选）
     */
    Result<List<NoteFavoriteDTO>> getUserFavorites(Long userId, Long folderId);
    
    /**
     * 获取用户的收藏夹列表
     */
    Result<List<FavoriteFolderDTO>> getUserFolders(Long userId);
    
    /**
     * 创建收藏夹
     */
    Result<FavoriteFolderDTO> createFolder(Long userId, CreateFolderRequest request);
    
    /**
     * 移动收藏到指定收藏夹
     */
    Result<Void> moveFavorite(Long favoriteId, Long userId, Long folderId);
    
    /**
     * 通过笔记ID移动收藏到指定收藏夹
     */
    Result<Void> moveFavoriteByNoteId(Long noteId, Long userId, Long folderId);
    
    /**
     * 删除收藏夹（笔记不会被取消收藏）
     */
    Result<Void> deleteFolder(Long folderId, Long userId);
    
    /**
     * 获取我的笔记被收藏的记录
     */
    Result<List<NoteFavoriteDTO>> getMyNoteFavorites(Long userId, Integer page, Integer size);
    
    /**
     * 获取我的笔记被收藏的未读数量
     */
    Result<Long> getMyNoteFavoriteUnreadCount(Long userId);
    
    /**
     * 标记收藏为已读
     */
    Result<Void> markFavoriteAsRead(Long favoriteId, Long userId);
}
