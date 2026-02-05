package com.onlystudents.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_order")
public class PaymentOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private Integer type; // 1-笔记购买, 2-订阅创作者, 3-打赏
    
    private Long targetId;
    
    private Integer targetType; // 1-笔记, 2-创作者
    
    private BigDecimal amount;
    
    private BigDecimal platformFee;
    
    private BigDecimal creatorAmount;
    
    private Integer status; // 0-待支付, 1-已支付, 2-支付失败, 3-已取消, 4-已退款
    
    private Integer payChannel; // 1-微信支付, 2-支付宝
    
    private LocalDateTime payTime;
    
    private String thirdPartyNo;
    
    private LocalDateTime expireTime;
    
    private String clientIp;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
