package com.onlystudents.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 笔记评分事件（接收自 rating-service）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteRatingEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long noteId;
    private Long userId;
    private Integer score;
    private Double averageScore;
    private Long ratingCount;
}
