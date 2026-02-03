package com.onlystudents.subscription.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("creator_subscription_config")
public class CreatorSubscriptionConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long creatorId;
    
    private BigDecimal price;
    
    private Integer isEnabled;
    
    private String benefits;
    
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
