package com.onlystudents.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 笔记评分事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteRatingEvent implements Serializable {
    
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
     * 评分
     */
    private Integer score;
    
    /**
     * 当前平均评分
     */
    private Double averageScore;
    
    /**
     * 当前评分人数
     */
    private Long ratingCount;
}
