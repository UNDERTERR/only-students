package com.onlystudents.search.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户搜索文档实体
 */
@Data
public class UserDocument {
    
    private Long userId;
    private String username;
    private String nickname;
    private String bio;
    private String avatar;
    private Integer educationLevel;
    private Long schoolId;
    private String schoolName;
    private Integer isCreator;
    private Integer followerCount;
    private Integer noteCount;
    private LocalDateTime createdAt;
}
