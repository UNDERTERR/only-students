package com.onlystudents.note.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 笔记收藏事件（接收自 rating-service）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteFavoriteEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long noteId;
    private Long userId;
    private Integer action; // 1-收藏 0-取消收藏
    private Long totalCount;
}
