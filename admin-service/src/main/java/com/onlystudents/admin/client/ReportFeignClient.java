package com.onlystudents.admin.client;

import com.onlystudents.admin.client.fallback.ReportFeignClientFallback;
import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "report-service", fallbackFactory = ReportFeignClientFallback.class)
public interface ReportFeignClient {

    @GetMapping("/report/stats")
    Result getReportStats();
}
