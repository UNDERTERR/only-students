package com.onlystudents.admin.client.fallback;

import com.onlystudents.admin.client.NoteFeignClient;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoteFeignClientFallback implements FallbackFactory<NoteFeignClient> {

    @Override
    public NoteFeignClient create(Throwable cause) {
        log.warn("note-service 调用失败: {}", cause.getMessage());
        return new NoteFeignClient() {
            @Override
            public Result getNoteStats() {
                return Result.success(null);
            }
        };
    }
}
