package com.onlystudents.comment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long noteId;
    
    private Long userId;
    
    private Long parentId;
    
    private Long rootId;
    
    private String content;
    
    private Integer likeCount;
    
    private Integer replyCount;
    
    private Integer status;
    
    private Integer isTop;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
