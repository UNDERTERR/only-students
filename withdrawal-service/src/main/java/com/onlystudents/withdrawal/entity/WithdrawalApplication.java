package com.onlystudents.withdrawal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("withdrawal_application")
public class WithdrawalApplication {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private BigDecimal amount;
    
    private Integer accountType;
    
    private String accountName;
    
    private String accountNumber;
    
    private String bankName;
    
    private String branchName;
    
    private Integer status;
    
    private Long auditorId;
    
    private String auditRemark;
    
    private LocalDateTime auditTime;
    
    private String transactionNo;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
