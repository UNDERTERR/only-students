package com.onlystudents.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message_notification")
public class MessageNotification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("from_user_id")
    private Long fromUserId;
    
    @TableField("to_user_id")
    private Long toUserId;
    
    @TableField("conversation_id")
    private Long conversationId;
    
    @TableField("message_id")
    private Long messageId;
    
    @TableField("is_read")
    private Integer isRead;
    
    @TableField("is_deleted")
    private Integer isDeleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
