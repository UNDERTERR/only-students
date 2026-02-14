package com.onlystudents.subscription.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    
    private Long id;
    
    private Long subscriberId;
    
    private Long creatorId;
    
    private LocalDateTime createdAt;
}
