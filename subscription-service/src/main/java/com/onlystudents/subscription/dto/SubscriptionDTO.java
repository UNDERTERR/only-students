package com.onlystudents.subscription.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    
    private Long id;
    
    private Long subscriberId;
    
    private Long creatorId;
    
    private Integer status;
    
    private LocalDateTime expireTime;
    
    private Long orderId;
    
    private BigDecimal price;
    
    private LocalDateTime createdAt;
}
