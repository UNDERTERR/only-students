package com.onlystudents.search.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserSearchResult {
    
    private Long id;
    
    private String username;
    
    private String nickname;
    
    private String avatar;
    
    private String bio;
    
    private Integer educationLevel;
    
    private String schoolName;
    
    private Integer isCreator;
    
    private Integer followerCount;
    
    private Integer noteCount;
    
    private LocalDateTime createdAt;
}
