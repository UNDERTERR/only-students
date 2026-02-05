package com.onlystudents.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 笔记收藏事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteFavoriteEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 笔记ID
     */
    private Long noteId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 事件类型：1-收藏 0-取消收藏
     */
    private Integer action;
    
    /**
     * 当前收藏总数
     */
    private Long totalCount;
}
