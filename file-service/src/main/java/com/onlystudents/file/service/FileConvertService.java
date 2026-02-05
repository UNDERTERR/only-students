package com.onlystudents.file.service;

public interface FileConvertService {
    
    void convertToPdf(Long sourceFileId);
    
    void processConvertTask(Long taskId);
}
