package com.onlystudents.note.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateNoteRequest {
    
    private String title;

    private String authorNickname;

    private String authorAvatar;

    private String content;
    
    private String coverImage;
    
    private Long categoryId;
    
    private Integer visibility; // 0-公开, 1-仅订阅可见, 2-仅付费可见, 3-订阅后付费可见, 4-仅自己可见
    
    private BigDecimal price;
    
    private String attachments;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
    
    private String subject;
    
    private List<String> tags;
}
