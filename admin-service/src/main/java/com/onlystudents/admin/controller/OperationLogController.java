package com.onlystudents.admin.controller;

import com.onlystudents.admin.entity.OperationLog;
import com.onlystudents.admin.service.OperationLogService;
import com.onlystudents.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/operation-log")
@RequiredArgsConstructor
@Tag(name = "操作日志", description = "管理员操作日志接口")
public class OperationLogController {
    
    private final OperationLogService operationLogService;
    
    @GetMapping("/list")
    @Operation(summary = "获取操作日志列表", description = "分页获取管理员操作日志")
    public Result<Map<String, Object>> getLogList(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "adminId", required = false) Long adminId,
            @RequestParam(name = "operationType", required = false) String operationType) {
        Map<String, Object> result = operationLogService.getLogList(page, size, adminId, operationType);
        return Result.success(result);
    }
    
    @PostMapping
    @Operation(summary = "记录操作日志", description = "记录管理员操作日志")
    public Result<Void> saveLog(@RequestBody Map<String, Object> logMap) {
        OperationLog log = new OperationLog();
        if (logMap.get("adminId") != null) {
            log.setAdminId(((Number) logMap.get("adminId")).longValue());
        }
        if (logMap.get("operationType") != null) {
            log.setOperationType(logMap.get("operationType").toString());
        }
        if (logMap.get("operationDesc") != null) {
            log.setOperationDesc(logMap.get("operationDesc").toString());
        }
        if (logMap.get("requestMethod") != null) {
            log.setRequestMethod(logMap.get("requestMethod").toString());
        }
        if (logMap.get("requestUrl") != null) {
            log.setRequestUrl(logMap.get("requestUrl").toString());
        }
        if (logMap.get("status") != null) {
            log.setStatus(((Number) logMap.get("status")).intValue());
        }
        operationLogService.saveLog(log);
        return Result.success();
    }
}
