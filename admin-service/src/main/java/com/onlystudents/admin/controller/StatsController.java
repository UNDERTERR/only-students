package com.onlystudents.admin.controller;

import com.onlystudents.admin.dto.response.AdminStatsResponse;
import com.onlystudents.admin.service.AdminStatsService;
import com.onlystudents.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@Tag(name = "全局统计", description = "全局统计数据接口")
public class StatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping
    @Operation(summary = "获取全局统计数据", description = "获取用户、笔记、举报等全局统计数据")
    public Result<AdminStatsResponse> getAdminStats() {
        return Result.success(adminStatsService.getAdminStats());
    }
}
