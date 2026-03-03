package com.onlystudents.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteNotificationDTO {
    private Long id;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private Long toUserId;
    private Long noteId;
    private String noteTitle;
    private String noteCoverImage;
    private Long favoriteId;
    private Integer isRead;
    private LocalDateTime createdAt;
}
