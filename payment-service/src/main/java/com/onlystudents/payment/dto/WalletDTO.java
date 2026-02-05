package com.onlystudents.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletDTO {
    
    private Long id;
    
    private Long userId;
    
    private BigDecimal balance;
    
    private BigDecimal frozenAmount;
    
    private BigDecimal totalIncome;
    
    private BigDecimal totalWithdrawal;
}
