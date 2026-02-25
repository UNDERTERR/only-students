package com.onlystudents.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("follower_notification")
public class FollowerNotification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("from_user_id")
    private Long fromUserId;
    
    @TableField("to_user_id")
    private Long toUserId;
    
    @TableField("subscription_id")
    private Long subscriptionId;
    
    @TableField("is_read")
    private Integer isRead;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
