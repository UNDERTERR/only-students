package com.onlystudents.note.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDTO {
    
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private String coverImage;
    
    private Long categoryId;
    
    private Integer visibility;
    
    private BigDecimal price;
    
    private Long originalFileId;
    
    private Long pdfFileId;
    
    private Integer status;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private Integer favoriteCount;
    
    private Integer commentCount;
    
    private Integer shareCount;
    
    private Double hotScore;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
    
    private String subject;
    
    private List<String> tags;
    
    private LocalDateTime publishTime;
    
    private LocalDateTime createdAt;
}
