package com.onlystudents.admin.client.fallback;

import com.onlystudents.admin.client.UserFeignClient;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserFeignClientFallback implements FallbackFactory<UserFeignClient> {

    @Override
    public UserFeignClient create(Throwable cause) {
        log.warn("user-service 调用失败: {}", cause.getMessage());
        return new UserFeignClient() {
            @Override
            public Result getUserStats() {
                return Result.success(null);
            }
        };
    }
}
