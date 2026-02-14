package com.onlystudents.subscription.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("creator_subscription_config")
public class CreatorSubscriptionConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long creatorId;
    
    private Integer isEnabled;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
