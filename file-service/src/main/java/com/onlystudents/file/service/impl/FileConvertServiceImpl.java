package com.onlystudents.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.file.config.MinioConfig;
import com.onlystudents.file.entity.FileConvertTask;
import com.onlystudents.file.entity.FileRecord;
import com.onlystudents.file.mapper.FileConvertTaskMapper;
import com.onlystudents.file.mapper.FileRecordMapper;
import com.onlystudents.file.service.FileConvertService;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件转换服务实现
 * 使用 Gotenberg HTTP API 进行文档转换（替代本地 LibreOffice）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileConvertServiceImpl implements FileConvertService {
    
    private final FileRecordMapper fileRecordMapper;
    private final FileConvertTaskMapper taskMapper;
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${gotenberg.url:http://localhost:3000}")
    private String gotenbergUrl;
    
    @Value("${file.convert.temp-dir:/tmp/only-students/convert}")
    private String tempDir;
    
    private static final Set<String> OFFICE_TYPES = new HashSet<>(Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp"
    ));
    
    @Override
    public void convertToPdf(Long sourceFileId) {
        FileRecord sourceFile = fileRecordMapper.selectById(sourceFileId);
        if (sourceFile == null) {
            log.error("源文件不存在: {}", sourceFileId);
            return;
        }
        
        String fileType = sourceFile.getFileType().toLowerCase();
        if (!OFFICE_TYPES.contains(fileType)) {
            log.info("文件类型[{}]无需转换", fileType);
            return;
        }
        
        // 检查是否已有转换任务
        FileConvertTask existTask = taskMapper.selectLatestTaskBySourceFileId(sourceFileId);
        if (existTask != null && existTask.getTaskStatus() == 0) {
            log.info("文件[{}]已有待处理的转换任务", sourceFileId);
            return;
        }
        
        // 创建转换任务
        FileConvertTask task = new FileConvertTask();
        task.setSourceFileId(sourceFileId);
        task.setConvertType(1);
        task.setTaskStatus(0);
        task.setRetryCount(0);
        taskMapper.insert(task);
        
        log.info("创建PDF转换任务: sourceFileId={}", sourceFileId);
    }
    
    @Override
    public void processConvertTask(Long taskId) {
        FileConvertTask task = taskMapper.selectById(taskId);
        if (task == null || task.getTaskStatus() != 0) {
            return;
        }
        
        FileRecord sourceFile = fileRecordMapper.selectById(task.getSourceFileId());
        if (sourceFile == null) {
            task.setTaskStatus(3);
            task.setErrorMsg("源文件不存在");
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            return;
        }
        
        File tempInputFile = null;
        File tempOutputFile = null;
        
        try {
            // 更新状态为处理中
            task.setTaskStatus(1);
            taskMapper.updateById(task);
            
            // 创建临时目录
            File tempDirFile = new File(tempDir);
            if (!tempDirFile.exists()) {
                tempDirFile.mkdirs();
            }
            
            // 从MinIO下载文件
            String tempInputPath = tempDir + "/" + IdUtil.simpleUUID() + "." + sourceFile.getFileType();
            String tempOutputPath = tempDir + "/" + IdUtil.simpleUUID() + ".pdf";
            
            tempInputFile = new File(tempInputPath);
            tempOutputFile = new File(tempOutputPath);
            
            try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(sourceFile.getFilePath())
                    .build())) {
                Files.copy(is, tempInputFile.toPath());
            }
            
            // 使用 Gotenberg HTTP API 转换
            convertWithGotenberg(tempInputFile, tempOutputFile);
            
            // 上传PDF到MinIO
            String pdfFileName = IdUtil.simpleUUID() + ".pdf";
            String pdfObjectName = "pdf/" + LocalDateTime.now().getYear() + "/" + pdfFileName;
            
            try (InputStream pdfIs = Files.newInputStream(tempOutputFile.toPath())) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(pdfObjectName)
                        .stream(pdfIs, tempOutputFile.length(), -1)
                        .contentType("application/pdf")
                        .build());
            }
            
            // 创建PDF文件记录
            FileRecord pdfRecord = new FileRecord();
            pdfRecord.setOriginalName(FileUtil.mainName(sourceFile.getOriginalName()) + ".pdf");
            pdfRecord.setFileName(pdfFileName);
            pdfRecord.setFilePath(pdfObjectName);
            pdfRecord.setFileSize(tempOutputFile.length());
            pdfRecord.setFileType("pdf");
            pdfRecord.setMimeType("application/pdf");
            pdfRecord.setUploaderId(sourceFile.getUploaderId());
            pdfRecord.setStorageType(1);
            pdfRecord.setStatus(1);
            fileRecordMapper.insert(pdfRecord);
            
            // 更新任务状态
            task.setTargetFileId(pdfRecord.getId());
            task.setTaskStatus(2);
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            
            // 更新原文件的PDF文件ID
            sourceFile.setPdfFileId(pdfRecord.getId());
            fileRecordMapper.updateById(sourceFile);
            
            log.info("PDF转换成功: sourceFileId={}, pdfFileId={}", sourceFile.getId(), pdfRecord.getId());
            
        } catch (Exception e) {
            log.error("PDF转换失败: taskId={}", taskId, e);
            task.setTaskStatus(3);
            task.setErrorMsg(e.getMessage());
            task.setRetryCount(task.getRetryCount() + 1);
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);
        } finally {
            // 清理临时文件
            if (tempInputFile != null && tempInputFile.exists()) {
                tempInputFile.delete();
            }
            if (tempOutputFile != null && tempOutputFile.exists()) {
                tempOutputFile.delete();
            }
        }
    }
    
    /**
     * 使用 Gotenberg HTTP API 转换文档
     * 
     * @param inputFile  输入文件（doc/docx/xls/xlsx等）
     * @param outputFile 输出PDF文件
     */
    private void convertWithGotenberg(File inputFile, File outputFile) throws Exception {
        log.info("调用 Gotenberg API 转换文件: {} -> PDF", inputFile.getName());
        
        // 构建请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", new FileSystemResource(inputFile));
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        // 调用 Gotenberg API
        String url = gotenbergUrl + "/forms/libreoffice/convert";
        ResponseEntity<byte[]> response = restTemplate.postForEntity(
                url, 
                requestEntity, 
                byte[].class
        );
        
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException(ResultCode.ERROR, 
                    "Gotenberg转换失败: HTTP " + response.getStatusCode());
        }
        
        // 保存PDF文件
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(response.getBody());
        }
        
        log.info("Gotenberg 转换完成: {} bytes", outputFile.length());
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
        } catch (Exception e) {
            // 桶初始化失败时，抛出运行时异常让服务启动失败（避免后续上传全报错）
            throw new RuntimeException("[MinIO 初始化失败] 桶 " + bucketName + " 创建失败：" + e.getMessage(), e);
        }
    }
}
