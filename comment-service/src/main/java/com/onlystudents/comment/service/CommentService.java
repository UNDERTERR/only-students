package com.onlystudents.comment.service;

import com.onlystudents.comment.dto.CommentDTO;
import com.onlystudents.comment.dto.CreateCommentRequest;

import java.util.List;

public interface CommentService {
    
    CommentDTO createComment(CreateCommentRequest request, Long userId);
    
    void deleteComment(Long commentId, Long userId);
    
    List<CommentDTO> getNoteComments(Long noteId, Long currentUserId);
    
    void likeComment(Long commentId, Long userId);
    
    void unlikeComment(Long commentId, Long userId);
    
    Integer getCommentCount(Long noteId);
    
    CommentDTO getCommentDetail(Long commentId, Long currentUserId);
    
    List<CommentDTO> getReceivedComments(Long userId, Integer page, Integer size);
    
    List<CommentDTO> getSentComments(Long userId, Integer page, Integer size);
    
    Integer getReceivedCommentUnreadCount(Long userId);
    
    void markCommentAsRead(Long commentId);
}
