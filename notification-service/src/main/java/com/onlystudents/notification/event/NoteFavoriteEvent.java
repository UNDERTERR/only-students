package com.onlystudents.notification.event;

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
    
    private Long noteId;
    
    private Long userId;
    
    private Integer action;
    
    private Long totalCount;
    
    private Long noteAuthorId;
    
    private String noteTitle;
}
