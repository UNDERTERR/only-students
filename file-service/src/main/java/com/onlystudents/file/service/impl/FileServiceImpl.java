package com.onlystudents.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.file.config.MinioConfig;
import com.onlystudents.file.dto.FileUploadResult;
import com.onlystudents.file.entity.FileRecord;
import com.onlystudents.file.enums.FileCategory;
import com.onlystudents.file.mapper.FileRecordMapper;
import com.onlystudents.file.service.FileConvertService;
import com.onlystudents.file.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final FileRecordMapper fileRecordMapper;
    private final FileConvertService fileConvertService;

    @Value("${file.upload.max-size:209715200}")
    private Long maxFileSize;

    @Value("${file.upload.allowed-types:doc,docx,xls,xlsx,ppt,pptx,pdf,txt,md,jpg,jpeg,png,gif}")
    private String allowedTypes;

    private static final Set<String> OFFICE_TYPES = new HashSet<>(Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    ));

    @Override
    public FileUploadResult uploadFile(MultipartFile file, Long userId) {
        // 默认上传到 private 目录
        return uploadFile(file, userId, FileCategory.PRIVATE);
    }
    
    @Override
    public FileUploadResult uploadFile(MultipartFile file, Long userId, FileCategory category) {
        // 计算MD5
        String md5Hash = calculateMd5(file);
        return uploadFileWithMd5Check(file, userId, md5Hash, category);
    }

    @Override
    public FileUploadResult uploadFileByVisibility(MultipartFile file, Long userId, Integer visibility) {
        FileCategory category = FileCategory.fromVisibility(visibility);
        return uploadFile(file, userId, category);
    }
    
    @Override
    public FileUploadResult uploadFileWithMd5Check(MultipartFile file, Long userId, String md5Hash) {
        return uploadFileWithMd5Check(file, userId, md5Hash, FileCategory.PRIVATE);
    }
    
    @Override
    public FileUploadResult uploadFileWithMd5Check(MultipartFile file, Long userId, String md5Hash, FileCategory category) {
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ResultCode.FILE_TOO_LARGE);
        }

        // 检查文件类型
        String originalName = file.getOriginalFilename();
        String fileType = FileUtil.extName(originalName).toLowerCase();
        if (!isAllowedType(fileType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的文件类型: " + fileType);
        }

        // MD5秒传检查
        FileRecord existFile = fileRecordMapper.selectByMd5Hash(md5Hash);
        if (existFile != null) {
            FileUploadResult result = new FileUploadResult();
            result.setFileId(existFile.getId());
            result.setOriginalName(existFile.getOriginalName());
            result.setFileName(existFile.getFileName());
            result.setFileSize(existFile.getFileSize());
            result.setFileType(existFile.getFileType());
            result.setMimeType(existFile.getMimeType());
            result.setMd5Hash(existFile.getMd5Hash());
            result.setIsDuplicate(true);
            result.setMessage("文件已存在，秒传成功");
            // 构造完整URL
            String fileUrl = minioConfig.getEndpoint() + "/"
                    + minioConfig.getBucketName() + "/"
                    + existFile.getFilePath();
            result.setFileUrl(fileUrl);
            return result;
        }

        // 根据分类确定存储路径前缀
        String prefix = getCategoryPrefix(category, userId);
        
        // 生成存储文件名
        String fileName = IdUtil.simpleUUID() + "." + fileType;
        String objectName = prefix + "/" + LocalDateTime.now().getYear() + "/" + LocalDateTime.now().getMonthValue() + "/" + fileName;
        
        log.info("[文件上传] 开始上传: bucket={}, objectName={}, fileType={}, size={}", 
                minioConfig.getBucketName(), objectName, fileType, file.getSize());
        
        // 记录访问级别
        boolean isPublic = (category == FileCategory.AVATARS || category == FileCategory.PUBLIC);

        try {
            // 上传到MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 保存文件记录
            FileRecord record = new FileRecord();
            record.setOriginalName(originalName);
            record.setFileName(fileName);
            record.setFilePath(objectName);
            record.setFileSize(file.getSize());
            record.setFileType(fileType);
            record.setMimeType(file.getContentType());
            record.setMd5Hash(md5Hash);
            record.setUploaderId(userId);
            record.setStorageType(1); // MinIO
            record.setStatus(1);
            record.setAccessLevel(isPublic ? 1 : 0); // 1-公开，0-私有

            fileRecordMapper.insert(record);

            // 如果是Office文件，异步转换PDF
            if (OFFICE_TYPES.contains(fileType)) {
                convertToPdf(record.getId());
            }

            FileUploadResult result = new FileUploadResult();
            result.setFileId(record.getId());
            result.setOriginalName(originalName);
            result.setFileName(fileName);
            result.setFileSize(file.getSize());
            result.setFileType(fileType);
            result.setMimeType(file.getContentType());
            result.setMd5Hash(md5Hash);
            result.setIsDuplicate(false);
            result.setMessage("上传成功");

            // 构造完整URL
            String fileUrl = minioConfig.getEndpoint() + "/"
                    + minioConfig.getBucketName() + "/"
                    + objectName;
            result.setFileUrl(fileUrl);

            return result;

        } catch (Exception e) {
            log.error("[文件上传] 上传失败: bucket={}, objectName={}, error={}", 
                    minioConfig.getBucketName(), objectName, e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "上传到MinIO失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileStream(Long fileId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null || record.getStatus() != 1) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return getFileStreamByPath(record.getFilePath());
    }

    @Override
    public InputStream getFileStream(String fileName) {
        FileRecord record = fileRecordMapper.selectByFileName(fileName);
        if (record == null || record.getStatus() != 1) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return getFileStreamByPath(record.getFilePath());
    }

    private InputStream getFileStreamByPath(String filePath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            log.error("获取文件流失败", e);
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public String getPreviewUrl(Long fileId, Long expireSeconds) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null || record.getStatus() != 1) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }

        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(record.getFilePath())
                    .expiry(expireSeconds.intValue(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("生成预览URL失败", e);
            throw new BusinessException(ResultCode.ERROR, "生成预览链接失败");
        }
    }

    @Override
    public void deleteFile(Long fileId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) {
            return;
        }

        try {
            // 从MinIO删除
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(record.getFilePath())
                    .build());

            // 使用 MyBatis Plus 逻辑删除，设置 deleted=1
            fileRecordMapper.deleteById(fileId);

        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new BusinessException(ResultCode.ERROR, "删除文件失败");
        }
    }

    @Override
    public void convertToPdf(Long fileId) {
        fileConvertService.convertToPdf(fileId);
    }

    /**
     * 根据文件分类获取存储路径前缀
     */
    private String getCategoryPrefix(FileCategory category, Long userId) {
        switch (category) {
            case AVATARS:
                return "avatars/" + userId;
            case PUBLIC:
                return "public";
            case PRIVATE:
                return "private/" + userId;
            case PAID:
                return "paid/" + userId;
            default:
                return "private/" + userId;
        }
    }

    private String calculateMd5(MultipartFile file) {
        try {
            return DigestUtil.md5Hex(file.getInputStream());
        } catch (Exception e) {
            log.error("计算MD5失败", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "文件校验失败");
        }
    }

    private boolean isAllowedType(String fileType) {
        Set<String> allowed = new HashSet<>(Arrays.asList(allowedTypes.split(",")));
        return allowed.contains(fileType.toLowerCase());
    }


    @PostConstruct
    public void initMinioBucket() {
        String endpoint = minioConfig.getEndpoint();
        String bucketName = minioConfig.getBucketName();
        String accessKey = minioConfig.getAccessKey();
        String secretKey = minioConfig.getSecretKey();
        // 1. 校验配置是否完整（避免空值导致初始化失败）
        if (endpoint == null || accessKey == null || secretKey == null || bucketName == null) {
            throw new RuntimeException("MinIO 配置不完整，请检查 application.yml 中的 minio 相关配置");
        }
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("[MinIO 初始化] 桶 " + bucketName + " 不存在，已自动创建");
            } else {
                log.info("[MinIO 初始化] 桶 " + bucketName + " 已存在，无需创建");
            }
            
            // 2. 配置细粒度访问策略
            setupBucketPolicy(bucketName);
            
        } catch (Exception e) {
            // 桶初始化失败时，抛出运行时异常让服务启动失败（避免后续上传全报错）
            throw new RuntimeException("[MinIO 初始化失败] 桶 " + bucketName + " 创建失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 配置 bucket 细粒度访问策略
     * - avatars/*: 公开可读（用户头像）
     * - public/*: 公开可读（笔记封面等公开资源）
     * - private/*: 公开可读（笔记附件，需要登录后查看）
     * - paid/*: 私有（付费内容）
     */
    private void setupBucketPolicy(String bucketName) throws Exception {
        String policyJson = "{\n" +
            "  \"Version\": \"2012-10-17\",\n" +
            "  \"Statement\": [\n" +
            "    {\n" +
            "      \"Effect\": \"Allow\",\n" +
            "      \"Principal\": \"*\",\n" +
            "      \"Action\": [\"s3:GetObject\"],\n" +
            "      \"Resource\": [\n" +
            "        \"arn:aws:s3:::" + bucketName + "/avatars/*\",\n" +
            "        \"arn:aws:s3:::" + bucketName + "/public/*\",\n" +
            "        \"arn:aws:s3:::" + bucketName + "/private/*\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        minioClient.setBucketPolicy(
            SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policyJson)
                .build()
        );
        log.info("[MinIO 策略配置] 已设置细粒度访问策略：\n" +
                "  - avatars/*: 公开可读\n" +
                "  - public/*: 公开可读\n" +
                "  - private/*: 公开可读\n" +
                "  - paid/*: 私有");
    }

}
