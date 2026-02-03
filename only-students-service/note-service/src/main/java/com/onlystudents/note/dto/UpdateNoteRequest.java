package com.onlystudents.note.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateNoteRequest {
    
    private String title;
    
    private String content;
    
    private String coverImage;
    
    private Long categoryId;
    
    private Integer visibility;
    
    private BigDecimal price;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
    
    private String subject;
    
    private List<String> tags;
}
