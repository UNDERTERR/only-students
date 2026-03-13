package com.onlystudents.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.report.dto.ReportStatsDTO;
import com.onlystudents.report.entity.Report;
import com.onlystudents.report.mapper.ReportMapper;
import com.onlystudents.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    
    private final ReportMapper reportMapper;
    
    @Override
    @Transactional
    public Report submitReport(Long reporterId, Long targetId, Integer targetType, String reason, 
                            String description, String evidence) {
        // 检查是否已举报过
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, reporterId);
        wrapper.eq(Report::getTargetId, targetId);
        wrapper.eq(Report::getTargetType, targetType);
        wrapper.eq(Report::getStatus, 0);
        
        if (reportMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "您已经举报过此内容，请勿重复举报");
        }
        
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetId(targetId);
        report.setTargetType(targetType);
        report.setReasonType(reason);
        report.setDescription(description);
        report.setEvidence(evidence);
        report.setStatus(0);
        
        reportMapper.insert(report);
        
        log.info("用户{}举报了目标{}(类型{})", reporterId, targetId, targetType);
        return report;
    }
    
    @Override
    public List<Report> getReportList(Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreatedAt);
        
        Page<Report> pageParam = new Page<>(page, size);
        return reportMapper.selectPage(pageParam, wrapper).getRecords();
    }
    
    @Override
    public List<Report> getMyReports(Long reporterId, Integer page, Integer size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, reporterId);
        wrapper.orderByDesc(Report::getCreatedAt);
        
        Page<Report> pageParam = new Page<>(page, size);
        return reportMapper.selectPage(pageParam, wrapper).getRecords();
    }
    
    @Override
    @Transactional
    public void processReport(Long reportId, Long handlerId, Integer status, String handleResult) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报不存在");
        }
        
        report.setStatus(status);
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setHandleTime(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        
        reportMapper.updateById(report);
        
        log.info("管理员{}处理了举报{}，结果：{}", handlerId, reportId, status);
    }
    
    @Override
    public Report getReportDetail(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报不存在");
        }
        return report;
    }
    
    @Override
    public ReportStatsDTO getReportStats() {
        ReportStatsDTO stats = new ReportStatsDTO();
        stats.setTotalReports(reportMapper.countTotalReports());
        stats.setPendingReports(reportMapper.countPendingReports());
        stats.setProcessingReports(reportMapper.countProcessingReports());
        stats.setProcessedReports(reportMapper.countProcessedReports());
        stats.setRejectedReports(reportMapper.countRejectedReports());
        stats.setTodayReports(reportMapper.countTodayReports());
        return stats;
    }
}
