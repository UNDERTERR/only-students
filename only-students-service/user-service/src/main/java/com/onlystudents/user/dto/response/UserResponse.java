package com.onlystudents.user.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    
    private Long id;
    
    private String username;
    
    private String email;
    
    private String phone;
    
    private String nickname;
    
    private String avatar;
    
    private String bio;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
    
    private Integer isCreator;
    
    private LocalDateTime lastLoginTime;
    
    private LocalDateTime createdAt;
}
