package com.onlystudents.subscription.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscribeRequest {
    
    private Long creatorId;
    
    private BigDecimal price;
}
