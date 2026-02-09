package com.onlystudents.file.dto;

import lombok.Data;

@Data
public class FileUploadResult {
    
    private Long fileId;
    
    private String originalName;
    
    private String fileName;
    
    private Long fileSize;
    
    private String fileType;
    
    private String mimeType;
    
    private String md5Hash;
    
    private Boolean isDuplicate;
    
    private String message;
    
    /**
     * 文件访问URL（完整路径）
     * 示例：http://localhost:9000/only-students-files/2024/2/uuid.jpg
     */
    private String fileUrl;
}
