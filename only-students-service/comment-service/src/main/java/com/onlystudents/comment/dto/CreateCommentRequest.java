package com.onlystudents.comment.dto;

import lombok.Data;

@Data
public class CreateCommentRequest {
    
    private Long noteId;
    
    private Long parentId; // 父评论ID，0表示顶级评论
    
    private Long rootId; // 根评论ID，用于楼中楼
    
    private String content;
}
