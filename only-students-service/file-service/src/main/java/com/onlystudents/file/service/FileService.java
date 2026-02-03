package com.onlystudents.file.service;

import com.onlystudents.file.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {
    
    FileUploadResult uploadFile(MultipartFile file, Long userId);
    
    FileUploadResult uploadFileWithMd5Check(MultipartFile file, Long userId, String md5Hash);
    
    InputStream getFileStream(Long fileId);
    
    InputStream getFileStream(String fileName);
    
    String getPreviewUrl(Long fileId, Long expireSeconds);
    
    void deleteFile(Long fileId);
    
    void convertToPdf(Long fileId);
}
