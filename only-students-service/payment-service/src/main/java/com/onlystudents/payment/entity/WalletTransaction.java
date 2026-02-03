package com.onlystudents.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_transaction")
public class WalletTransaction {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Integer type; // 1-收入, 2-支出, 3-提现, 4-退款
    
    private BigDecimal amount;
    
    private BigDecimal balance;
    
    private String relatedOrderNo;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
