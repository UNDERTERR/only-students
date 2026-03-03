package com.onlystudents.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    private Integer type;
    
    private String title;
    
    private String content;
    
    @TableField("target_id")
    private Long targetId;
    
    @TableField("target_type")
    private Integer targetType;
    
    @TableField("extra_data")
    private String extraData;
    
    @TableField("is_read")
    private Integer isRead;
    
    @TableField("read_time")
    private LocalDateTime readTime;
    
    @TableField("send_channel")
    private Integer sendChannel;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
