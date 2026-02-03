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
}
