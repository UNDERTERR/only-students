package com.onlystudents.file.service.impl;

import cn.hutool.core.util.IdUtil;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.file.config.MinioConfig;
import com.onlystudents.file.entity.FileConvertTask;
import com.onlystudents.file.entity.FileRecord;
import com.onlystudents.file.mapper.FileConvertTaskMapper;
import com.onlystudents.file.mapper.FileRecordMapper;
import com.onlystudents.file.service.FileConvertService;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileConvertServiceImpl implements FileConvertService {
    
    private final FileRecordMapper fileRecordMapper;
    private final FileConvertTaskMapper taskMapper;
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    
    @Value("${file.convert.temp-dir:/tmp/file-convert}")
    private String tempDir;
    
    @Value("${gotenberg.url:http://localhost:3000}")
    private String gotenbergUrl;
    
    @Value("${file.convert.enabled:true}")
    private boolean convertEnabled;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final Set<String> OFFICE_TYPES = new HashSet<>(Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    ));
    
    private static final Set<String> TEXT_TYPES = new HashSet<>(Arrays.asList(
            "txt", "md", "json", "xml", "csv", "log"
    ));
    
    @PostConstruct
    public void init() {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
        } catch (Exception e) {
            log.info("[MinIO 初始化] 桶已存在");
        }
    }
    
    @Override
    public void convertToPdf(Long sourceFileId) {
        log.info("收到PDF转换请求: sourceFileId={}", sourceFileId);
        
        FileRecord sourceFile = fileRecordMapper.selectById(sourceFileId);
        if (sourceFile == null) {
            log.error("源文件不存在: {}", sourceFileId);
            return;
        }
        
        String fileType = sourceFile.getFileType().toLowerCase();
        log.info("源文件类型: {}", fileType);
        
        // 文本类型直接存储，无需转换
        if (TEXT_TYPES.contains(fileType)) {
            log.info("文件类型[{}]为文本格式，直接存储", fileType);
            return;
        }
        
        // Office类型需要转换PDF
        if (!OFFICE_TYPES.contains(fileType)) {
            log.info("文件类型[{}]无需转换", fileType);
            return;
        }
        
        // 检查是否已有待处理的转换任务
        FileConvertTask existTask = taskMapper.selectLatestTaskBySourceFileId(sourceFileId);
        if (existTask != null && existTask.getTaskStatus() == 0) {
            log.info("文件[{}]已有待处理的转换任务", sourceFileId);
            return;
        }
        
        // 如果已有完成或失败的任务，创建新任务重试
        if (existTask != null && (existTask.getTaskStatus() == 2 || existTask.getTaskStatus() == 3)) {
            log.info("文件[{}]已有转换记录(status={})，创建新任务重试", sourceFileId, existTask.getTaskStatus());
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
        log.info("开始处理转换任务: taskId={}", taskId);
        
        FileConvertTask task = taskMapper.selectById(taskId);
        if (task == null || task.getTaskStatus() != 0) {
            log.warn("任务不存在或状态不对: taskId={}, status={}", taskId, task != null ? task.getTaskStatus() : null);
            return;
        }
        
        FileRecord sourceFile = fileRecordMapper.selectById(task.getSourceFileId());
        if (sourceFile == null) {
            log.error("源文件不存在: {}", task.getSourceFileId());
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
            tempInputFile = new File(tempInputPath);
            
            try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(sourceFile.getFilePath())
                    .build())) {
                java.nio.file.Files.copy(is, tempInputFile.toPath());
            }
            
            log.info("从MinIO下载文件成功: {}", tempInputPath);
            
            // 调用Gotenberg转换
            String gotenbergConvertUrl = gotenbergUrl + "/forms/libreoffice/convert";
            log.info("调用Gotenberg转换: {}", gotenbergConvertUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            FileSystemResource resource = new FileSystemResource(tempInputFile);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            byte[] pdfBytes = restTemplate.postForObject(gotenbergConvertUrl, requestEntity, byte[].class);
            
            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new BusinessException(ResultCode.ERROR, "Gotenberg转换返回空结果");
            }
            
            log.info("Gotenberg转换成功，PDF大小: {} bytes", pdfBytes.length);
            
            // 保存PDF到临时文件
            String tempOutputPath = tempDir + "/" + IdUtil.simpleUUID() + ".pdf";
            tempOutputFile = new File(tempOutputPath);
            try (FileOutputStream fos = new FileOutputStream(tempOutputFile)) {
                fos.write(pdfBytes);
            }
            
            // 上传PDF到MinIO
            String pdfObjectName = sourceFile.getFilePath().replaceAll("\\.[^.]+$", ".pdf");
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(pdfObjectName)
                    .stream(new FileInputStream(tempOutputFile), tempOutputFile.length(), -1)
                    .contentType("application/pdf")
                    .build());
            
            log.info("PDF上传到MinIO成功: {}", pdfObjectName);
            
            // 保存PDF文件记录
            FileRecord pdfRecord = new FileRecord();
            pdfRecord.setFileName(tempOutputFile.getName());
            pdfRecord.setOriginalName(sourceFile.getOriginalName().replaceAll("\\.[^.]+$", ".pdf"));
            pdfRecord.setFilePath(pdfObjectName);
            pdfRecord.setFileType("pdf");
            pdfRecord.setMimeType("application/pdf");
            pdfRecord.setFileSize(tempOutputFile.length());
            pdfRecord.setUploaderId(sourceFile.getUploaderId());
            pdfRecord.setStorageType(1);
            pdfRecord.setStatus(1);
            pdfRecord.setAccessLevel(sourceFile.getAccessLevel());
            fileRecordMapper.insert(pdfRecord);
            
            // 更新转换任务状态
            task.setTargetFileId(pdfRecord.getId());
            task.setTaskStatus(2);
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            
            log.info("PDF转换成功: sourceFileId={}, pdfFileId={}", task.getSourceFileId(), pdfRecord.getId());
            
        } catch (Exception e) {
            log.error("PDF转换失败: sourceFileId={}", task.getSourceFileId(), e);
            task.setTaskStatus(3);
            task.setErrorMsg(e.getMessage());
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
    
    @Scheduled(fixedDelay = 30000) // 每30秒检查一次
    public void processPendingTasks() {
        log.info("开始检查待转换任务...");
        try {
            List<FileConvertTask> pendingTasks = taskMapper.selectPendingTasks();
            log.info("发现 {} 个待转换任务", pendingTasks.size());
            for (FileConvertTask task : pendingTasks) {
                log.info("处理待转换任务: taskId={}, sourceFileId={}", task.getId(), task.getSourceFileId());
                processConvertTask(task.getId());
            }
        } catch (Exception e) {
            log.error("处理待转换任务失败", e);
        }
    }
    
    @Override
    public Integer getConvertStatus(Long sourceFileId) {
        FileConvertTask task = taskMapper.selectLatestTaskBySourceFileId(sourceFileId);
        if (task == null) {
            return 0;
        }
        return task.getTaskStatus();
    }
    
    @Override
    public Long getPdfFileId(Long sourceFileId) {
        FileConvertTask task = taskMapper.selectLatestTaskBySourceFileId(sourceFileId);
        if (task != null && task.getTaskStatus() == 2 && task.getTargetFileId() != null) {
            return task.getTargetFileId();
        }
        return null;
    }
}
