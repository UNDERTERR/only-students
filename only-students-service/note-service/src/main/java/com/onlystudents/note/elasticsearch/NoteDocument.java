package com.onlystudents.note.elasticsearch;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDocument {
    
    private Long noteId;
    private String title;
    private String content;
    private List<String> tags;
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private String username;
    private String nickname;
    private Integer educationLevel;
    private Long schoolId;
    private String schoolName;
    private String subject;
    private Integer visibility;
    private BigDecimal price;
    private Integer status;
    private Double hotScore;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer shareCount;
    private Float ratingAvg;
    private String coverImage;
    private LocalDateTime publishTime;
    private LocalDateTime createdAt;
}
