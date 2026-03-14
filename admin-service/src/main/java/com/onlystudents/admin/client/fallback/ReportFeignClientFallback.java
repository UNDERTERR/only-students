package com.onlystudents.admin.client.fallback;

import com.onlystudents.admin.client.ReportFeignClient;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportFeignClientFallback implements FallbackFactory<ReportFeignClient> {

    @Override
    public ReportFeignClient create(Throwable cause) {
        log.warn("report-service 调用失败: {}", cause.getMessage());
        return new ReportFeignClient() {
            @Override
            public Result getReportStats() {
                return Result.success(null);
            }
        };
    }
}
