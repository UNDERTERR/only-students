package com.onlystudents.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_convert_task")
public class FileConvertTask {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sourceFileId;
    
    private Long targetFileId;
    
    private Integer convertType;
    
    private Integer taskStatus;
    
    private String errorMsg;
    
    private Integer retryCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
}
