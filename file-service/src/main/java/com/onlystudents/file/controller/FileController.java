package com.onlystudents.file.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.file.dto.FileUploadResult;
import com.onlystudents.file.enums.FileCategory;
import com.onlystudents.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、预览等接口")
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "支持Office文档、PDF、图片等格式，最大200MB")
    public Result<FileUploadResult> uploadFile(@RequestParam("file") MultipartFile file,
                                                @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                @RequestParam(required = false) String category) {
        FileCategory fileCategory = parseCategory(category);
        return Result.success(fileService.uploadFile(file, userId, fileCategory));
    }
    
    @PostMapping("/upload-with-check")
    @Operation(summary = "上传文件（带MD5秒传）", description = "前端计算MD5，后端检查是否已存在相同文件")
    public Result<FileUploadResult> uploadFileWithCheck(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("md5") String md5Hash,
                                                         @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                         @RequestParam(required = false) String category) {
        FileCategory fileCategory = parseCategory(category);
        return Result.success(fileService.uploadFileWithMd5Check(file, userId, md5Hash, fileCategory));
    }
    
    @PostMapping("/upload/avatar")
    @Operation(summary = "上传头像", description = "专门用于上传用户头像，自动存放到公开目录")
    public Result<FileUploadResult> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(fileService.uploadFile(file, userId, FileCategory.AVATARS));
    }
    
    /**
     * 解析文件分类
     */
    private FileCategory parseCategory(String category) {
        if (category == null || category.isEmpty()) {
            return FileCategory.PRIVATE;
        }
        try {
            return FileCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FileCategory.PRIVATE;
        }
    }
    
    @GetMapping("/preview/{fileId}")
    @Operation(summary = "预览文件", description = "获取文件预览URL，有效期默认1小时")
    public Result<String> getPreviewUrl(@PathVariable Long fileId,
                                         @RequestParam(defaultValue = "3600") Long expireSeconds) {
        return Result.success(fileService.getPreviewUrl(fileId, expireSeconds));
    }
    
    @GetMapping("/download/{fileId}")
    @Operation(summary = "下载文件", description = "下载原始文件")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
        try (InputStream is = fileService.getFileStream(fileId)) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("file", StandardCharsets.UTF_8));
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, len);
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }
    
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "逻辑删除文件记录")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return Result.success();
    }
}
