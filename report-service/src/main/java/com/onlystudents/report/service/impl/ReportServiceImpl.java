package com.onlystudents.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.report.client.CommentFeignClient;
import com.onlystudents.report.client.NoteFeignClient;
import com.onlystudents.report.client.OperationLogFeignClient;
import com.onlystudents.report.client.UserFeignClient;
import com.onlystudents.report.dto.ReportStatsDTO;
import com.onlystudents.report.entity.Report;
import com.onlystudents.report.mapper.ReportMapper;
import com.onlystudents.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    
    private final ReportMapper reportMapper;
    private final UserFeignClient userFeignClient;
    private final NoteFeignClient noteFeignClient;
    private final CommentFeignClient commentFeignClient;
    private final OperationLogFeignClient operationLogFeignClient;
    
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
        
        String operationDesc = "";
        
        // 如果举报通过 (status=2)，执行相应的处理
        if (status == 2) {
            Integer targetType = report.getTargetType();
            Long targetId = report.getTargetId();
            
            try {
                if (targetType == 1) {
                    // 举报类型为笔记：将笔记状态改为草稿
                    noteFeignClient.setNoteToDraft(targetId);
                    operationDesc = "举报处理：笔记ID " + targetId + " 设为草稿";
                    log.info("举报处理：笔记{}已设置为草稿状态", targetId);
                } else if (targetType == 2) {
                    // 举报类型为评论：物理删除评论
                    commentFeignClient.deleteComment(targetId);
                    operationDesc = "举报处理：评论ID " + targetId + " 已删除";
                    log.info("举报处理：评论{}已物理删除", targetId);
                } else if (targetType == 3) {
                    // 举报类型为用户：封禁用户（默认封禁30天）
                    LocalDateTime banTime = LocalDateTime.now().plusDays(30);
                    userFeignClient.setUserBan(targetId, banTime, report.getReasonType());
                    operationDesc = "举报处理：用户ID " + targetId + " 封禁30天";
                    log.info("举报处理：用户{}已被封禁，截止时间: {}", targetId, banTime);
                }
            } catch (Exception e) {
                log.error("举报处理执行失败: targetType={}, targetId={}", targetType, targetId, e);
                throw new BusinessException(500, "处理举报失败: " + e.getMessage());
            }
        }
        
        report.setStatus(status);
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setHandleTime(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        
        reportMapper.updateById(report);
        
        log.info("管理员{}处理了举报{}，结果：{}", handlerId, reportId, status);
        
        // 记录操作日志
        try {
            java.util.Map<String, Object> logMap = new java.util.HashMap<>();
            logMap.put("adminId", handlerId);
            logMap.put("operationType", "PROCESS_REPORT");
            logMap.put("operationDesc", operationDesc + ", 处理结果: " + (status == 2 ? "通过" : "拒绝"));
            logMap.put("requestMethod", "POST");
            logMap.put("requestUrl", "/report/process/" + reportId);
            logMap.put("status", 1);
            operationLogFeignClient.saveLog(logMap);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
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
    
    @Override
    public Long countReports(Integer status) {
        return reportMapper.countByStatus(status);
    }
}
