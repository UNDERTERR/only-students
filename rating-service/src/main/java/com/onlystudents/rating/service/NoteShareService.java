package com.onlystudents.rating.service;

import com.onlystudents.common.result.Result;
import com.onlystudents.rating.dto.NoteShareDTO;

import java.util.List;

/**
 * 笔记分享服务
 */
public interface NoteShareService {
    
    /**
     * 创建分享
     */
    Result<NoteShareDTO> createShare(Long noteId, Long userId, Integer shareType);
    
    /**
     * 根据分享码获取分享信息
     */
    Result<NoteShareDTO> getShareByCode(String shareCode);
    
    /**
     * 增加点击次数
     */
    Result<Void> incrementClickCount(String shareCode);
    
    /**
     * 获取笔记分享数
     */
    Result<Long> getShareCount(Long noteId);
    
    /**
     * 获取用户分享列表
     */
    Result<List<NoteShareDTO>> getUserShares(Long userId);
}
