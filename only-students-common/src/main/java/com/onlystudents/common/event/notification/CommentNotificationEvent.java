package com.onlystudents.common.event.notification;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentNotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long commentId;
    private Long fromUserId;
    private Long toUserId;
    private Long noteId;
    private String commentContent;
    private String noteTitle;
    
    public CommentNotificationEvent() {}
    
    public CommentNotificationEvent(Long commentId, Long fromUserId, Long toUserId, Long noteId, String commentContent, String noteTitle) {
        this.commentId = commentId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.noteId = noteId;
        this.commentContent = commentContent;
        this.noteTitle = noteTitle;
    }
}
