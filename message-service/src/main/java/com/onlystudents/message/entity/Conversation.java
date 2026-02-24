package com.onlystudents.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id_1")
    private Long userId1;
    
    @TableField("user_id_2")
    private Long userId2;
    
    @TableField("last_message_id")
    private Long lastMessageId;
    
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;
    
    @TableField("last_message_preview")
    private String lastMessagePreview;
    
    @TableField("user_1_unread_count")
    private Integer user1UnreadCount;
    
    @TableField("user_2_unread_count")
    private Integer user2UnreadCount;
    
    @TableField("user_1_hidden")
    private Integer user1Hidden;
    
    @TableField("user_2_hidden")
    private Integer user2Hidden;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // ========== 非数据库字段 ==========
    
    /**
     * 当前用户视角下的目标用户ID
     */
    @TableField(exist = false)
    private Long targetUserId;

    /**
     * 当前用户视角下的目标用户昵称
     */
    @TableField(exist = false)
    private String targetNickname;
    
    /**
     * 当前用户视角下的目标用户头像
     */
    @TableField(exist = false)
    private String targetUserAvatar;
    
    /**
     * 当前用户视角下的未读数
     */
    @TableField(exist = false)
    private Integer unreadCount;
    
    /**
     * 最后一条消息内容（用于前端显示）
     */
    @TableField(exist = false)
    private String lastMessage;
}
