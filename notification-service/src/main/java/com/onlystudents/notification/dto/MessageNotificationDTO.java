package com.onlystudents.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageNotificationDTO {
    private Long id;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private Long toUserId;
    private Long conversationId;
    private Long messageId;
    private String lastMessageContent;
    private Integer isRead;
    private Integer isDeleted;
    private LocalDateTime createdAt;
}
