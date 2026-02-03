package com.onlystudents.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    
    private Integer type; // 1-笔记购买, 2-订阅创作者, 3-打赏
    
    private Long targetId;
    
    private Integer targetType;
    
    private BigDecimal amount;
    
    private Integer payChannel; // 1-微信支付, 2-支付宝
    
    private String remark;
}
