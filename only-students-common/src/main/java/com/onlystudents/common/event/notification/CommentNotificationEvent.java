package com.onlystudents.common.event.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentNotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long commentId;
    private Long fromUserId;
    private Long toUserId;
    private Long noteId;
    private String commentContent;
    private String noteTitle;

}
