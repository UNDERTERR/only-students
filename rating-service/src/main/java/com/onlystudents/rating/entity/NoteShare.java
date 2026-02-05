package com.onlystudents.rating.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("note_share")
public class NoteShare {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long noteId;
    
    private Long userId;
    
    private Integer shareType;
    
    private String shareCode;
    
    private Integer clickCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
