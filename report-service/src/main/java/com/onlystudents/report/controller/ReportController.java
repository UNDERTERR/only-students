package com.onlystudents.report.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.report.entity.Report;
import com.onlystudents.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = "举报管理", description = "提交举报、处理举报等接口")
public class ReportController {
    
    private final ReportService reportService;
    
    @PostMapping("/submit")
    @Operation(summary = "提交举报", description = "提交对笔记、评论或用户的举报")
    public Result<Report> submitReport(@RequestHeader(CommonConstants.USER_ID_HEADER) Long reporterId,
                                      @RequestParam(name = "targetId") Long targetId,
                                      @RequestParam(name = "targetType") Integer targetType,
                                      @RequestParam(name = "reason") Integer reason,
                                      @RequestParam(name = "description", required = false) String description,
                                      @RequestParam(name = "evidence", required = false) String evidence) {
        return Result.success(reportService.submitReport(reporterId, targetId, targetType, reason, description, evidence));
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取举报列表", description = "管理员获取举报列表，可按状态筛选")
    public Result<List<Report>> getReportList(@RequestParam(name = "status", required = false) Integer status,
                                              @RequestParam(name = "page", defaultValue = "1") Integer page,
                                              @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(reportService.getReportList(status, page, size));
    }
    
    @GetMapping("/my")
    @Operation(summary = "获取我的举报", description = "获取当前用户提交的所有举报")
    public Result<List<Report>> getMyReports(@RequestHeader(CommonConstants.USER_ID_HEADER) Long reporterId,
                                           @RequestParam(name = "page", defaultValue = "1") Integer page,
                                           @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(reportService.getMyReports(reporterId, page, size));
    }
    
    @PostMapping("/process/{reportId}")
    @Operation(summary = "处理举报", description = "管理员处理举报")
    public Result<Void> processReport(@PathVariable Long reportId,
                                     @RequestHeader(CommonConstants.USER_ID_HEADER) Long handlerId,
                                     @RequestParam(name = "status") Integer status,
                                     @RequestParam(name = "handleResult") String handleResult) {
        reportService.processReport(reportId, handlerId, status, handleResult);
        return Result.success();
    }
    
    @GetMapping("/{reportId}")
    @Operation(summary = "获取举报详情", description = "获取指定举报的详细信息")
    public Result<Report> getReportDetail(@PathVariable Long reportId) {
        return Result.success(reportService.getReportDetail(reportId));
    }
}
