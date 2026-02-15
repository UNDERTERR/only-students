package com.onlystudents.file.service;

public interface FileConvertService {
    
    void convertToPdf(Long sourceFileId);
    
    void processConvertTask(Long taskId);
    
    /**
     * 获取文件转换状态
     * @param sourceFileId 源文件ID
     * @return 转换状态: 0=无转换任务, 1=待处理, 2=处理中, 3=转换成功(可预览PDF), 4=转换失败
     */
    Integer getConvertStatus(Long sourceFileId);
    
    /**
     * 获取转换后的PDF文件ID
     * @param sourceFileId 源文件ID
     * @return PDF文件ID（如果转换成功）
     */
    Long getPdfFileId(Long sourceFileId);
}
