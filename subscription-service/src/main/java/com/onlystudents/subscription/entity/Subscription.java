package com.onlystudents.subscription.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("subscription")
public class Subscription {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long subscriberId;
    
    private Long creatorId;
    
    private Integer status;
    
    private LocalDateTime expireTime;
    
    private Long orderId;
    
    private BigDecimal price;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
