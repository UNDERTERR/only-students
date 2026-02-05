package com.onlystudents.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayCallbackRequest {
    
    private String orderNo;
    
    private String thirdPartyNo;
    
    private Integer status; // 1-成功, 2-失败
    
    private BigDecimal amount;
    
    private String sign;
}
