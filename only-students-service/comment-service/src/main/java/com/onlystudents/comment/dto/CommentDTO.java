package com.onlystudents.comment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    
    private Long id;
    
    private Long noteId;
    
    private Long userId;
    
    private String username;
    
    private String avatar;
    
    private Long parentId;
    
    private Long rootId;
    
    private String content;
    
    private Integer likeCount;
    
    private Integer replyCount;
    
    private Boolean isLiked;
    
    private List<CommentDTO> replies;
    
    private LocalDateTime createdAt;
}
