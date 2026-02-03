package com.onlystudents.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_record")
public class FileRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String originalName;
    
    private String fileName;
    
    private String filePath;
    
    private Long fileSize;
    
    private String fileType;
    
    private String mimeType;
    
    private String md5Hash;
    
    private Long uploaderId;
    
    private Integer storageType;
    
    private Integer status;
    
    private LocalDateTime expireTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
