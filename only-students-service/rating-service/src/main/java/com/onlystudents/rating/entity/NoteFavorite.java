package com.onlystudents.rating.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("note_favorite")
public class NoteFavorite {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long noteId;
    
    private Long userId;
    
    private Long folderId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
