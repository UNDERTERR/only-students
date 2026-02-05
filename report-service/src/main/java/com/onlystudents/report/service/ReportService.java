package com.onlystudents.report.service;

import com.onlystudents.report.entity.Report;

import java.util.List;

public interface ReportService {
    
    Report submitReport(Long reporterId, Long targetId, Integer targetType, Integer reason, String description, String evidence);
    
    List<Report> getReportList(Integer status, Integer page, Integer size);
    
    List<Report> getMyReports(Long reporterId, Integer page, Integer size);
    
    void processReport(Long reportId, Long handlerId, Integer status, String handleResult);
    
    Report getReportDetail(Long reportId);
}
