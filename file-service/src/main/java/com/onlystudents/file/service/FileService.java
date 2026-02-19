package com.onlystudents.file.service;

import com.onlystudents.file.dto.FileUploadResult;
import com.onlystudents.file.enums.FileCategory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {
    
    /**
     * 上传文件（默认上传到 private 目录）
     */
    FileUploadResult uploadFile(MultipartFile file, Long userId);
    
    /**
     * 上传文件到指定分类
     */
    FileUploadResult uploadFile(MultipartFile file, Long userId, FileCategory category);

    /**
     * 根据笔记可见性上传文件
     * @param file 文件
     * @param userId 用户ID
     * @param visibility 笔记可见性: 0=公开, 1=订阅可见, 2=付费可见, 3=订阅+付费, 4=仅自己
     */
    FileUploadResult uploadFileByVisibility(MultipartFile file, Long userId, Integer visibility);
    
    /**
     * 带MD5校验的文件上传（默认上传到 private 目录）
     */
    FileUploadResult uploadFileWithMd5Check(MultipartFile file, Long userId, String md5Hash);
    
    /**
     * 带MD5校验的文件上传到指定分类
     */
    FileUploadResult uploadFileWithMd5Check(MultipartFile file, Long userId, String md5Hash, FileCategory category);
    
    InputStream getFileStream(Long fileId);
    
    InputStream getFileStream(String fileName);
    
    String getPreviewUrl(Long fileId, Long expireSeconds);
    
    void deleteFile(Long fileId);
    
    void convertToPdf(Long fileId);

    /**
     * 获取文件转换状态
     * @param fileId 源文件ID
     * @return 转换状态: 0=无转换任务, 1=待处理, 2=处理中, 3=转换成功, 4=转换失败
     */
    Integer getConvertStatus(Long fileId);

    /**
     * 获取转换后的PDF文件ID
     * @param sourceFileId 源文件ID
     * @return PDF文件ID（如果转换成功）
     */
    Long getPdfFileId(Long sourceFileId);
}
