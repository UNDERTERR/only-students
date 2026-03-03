package com.onlystudents.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowerNotificationDTO {
    private Long id;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private Long toUserId;
    private Integer isRead;
    private LocalDateTime createdAt;
}
