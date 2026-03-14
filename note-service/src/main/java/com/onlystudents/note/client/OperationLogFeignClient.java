package com.onlystudents.note.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志 Feign 客户端
 * 用于 note-service 调用 admin-service 记录操作日志
 */
@FeignClient(name = "admin-service")
public interface OperationLogFeignClient {

    /**
     * 记录操作日志
     */
    @PostMapping("/admin/operation-log")
    Result<Void> saveLog(@RequestBody Object log);
}
