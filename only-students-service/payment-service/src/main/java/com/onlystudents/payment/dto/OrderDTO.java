package com.onlystudents.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private Integer type;
    
    private Long targetId;
    
    private Integer targetType;
    
    private BigDecimal amount;
    
    private BigDecimal platformFee;
    
    private BigDecimal creatorAmount;
    
    private Integer status;
    
    private Integer payChannel;
    
    private LocalDateTime payTime;
    
    private LocalDateTime expireTime;
    
    private LocalDateTime createdAt;
}
