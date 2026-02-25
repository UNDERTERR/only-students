package com.onlystudents.common.event.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteNotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long favoriteId;
    private Long fromUserId;
    private Long toUserId;
    private Long noteId;
    private String noteTitle;
    private String noteCoverImage;
}
