package com.onlystudents.note.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateNoteRequest {
    
    private String title;
    
    private String content;
    
    private String coverImage;
    
    private Long categoryId;
    
    private Integer visibility; // 0-公开, 1-订阅者, 2-付费
    
    private BigDecimal price;
    
    private Long originalFileId;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
    
    private String subject;
    
    private List<String> tags;
}
