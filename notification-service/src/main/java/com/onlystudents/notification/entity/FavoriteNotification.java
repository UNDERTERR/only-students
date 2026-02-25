package com.onlystudents.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite_notification")
public class FavoriteNotification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("from_user_id")
    private Long fromUserId;
    
    @TableField("to_user_id")
    private Long toUserId;
    
    @TableField("note_id")
    private Long noteId;
    
    @TableField("favorite_id")
    private Long favoriteId;
    
    @TableField("is_read")
    private Integer isRead;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
