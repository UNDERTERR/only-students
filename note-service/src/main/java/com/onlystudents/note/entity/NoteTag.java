package com.onlystudents.note.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("note_tag")
public class NoteTag {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer usageCount;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
