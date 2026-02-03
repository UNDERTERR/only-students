package com.onlystudents.search.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteSearchResult {
    
    private Long id;
    
    private String title;
    
    private String summary;
    
    private Long authorId;
    
    private String authorName;
    
    private String authorAvatar;
    
    private Integer price;
    
    private Integer status;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private Integer downloadCount;
    
    private List<String> tags;
    
    private LocalDateTime createdAt;
}
