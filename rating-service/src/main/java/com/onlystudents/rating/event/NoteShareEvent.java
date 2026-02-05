package com.onlystudents.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 笔记分享事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteShareEvent implements Serializable {
    
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
     * 分享类型
     */
    private Integer shareType;
    
    /**
     * 分享码
     */
    private String shareCode;
    
    /**
     * 当前分享总数
     */
    private Long totalCount;
}
