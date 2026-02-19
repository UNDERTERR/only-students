package com.onlystudents.message.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息DTO（用于Feign调用）
 */
@Data
public class UserInfoDTO {
    
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
