package com.onlystudents.search.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteSearchResult {
    
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String coverImage;
    private Integer visibility;
    private Integer price;
    private Boolean isFree;
    private Integer viewCount;
    private Integer ratingCount;
    private Integer averageRating;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer status;
    
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    
    private List<String> tags;
    
    private LocalDateTime createdAt;
}
