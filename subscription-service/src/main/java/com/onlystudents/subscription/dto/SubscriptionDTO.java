package com.onlystudents.subscription.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    
    private Long id;
    
    private Long subscriberId;
    
    private Long creatorId;
    
    private LocalDateTime createdAt;
    
    // 创作者信息（订阅页面使用）
    private String creatorNickname;
    
    private String creatorAvatar;
    
    private String creatorBio;
    
    // 订阅者信息（粉丝页面使用）
    private String subscriberNickname;
    
    private String subscriberAvatar;
    
    private String subscriberBio;
}
