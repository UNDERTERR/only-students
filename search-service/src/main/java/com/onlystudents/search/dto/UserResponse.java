package com.onlystudents.search.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户响应DTO（用于 Search-Service 调用 User-Service）
 */
@Data
public class UserResponse {
    
    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
    private String email;
    private String phone;
    private Integer educationLevel;
    private Long schoolId;
    private String schoolName;
    private Integer isCreator;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
