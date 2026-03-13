package com.onlystudents.admin.client;

import com.onlystudents.admin.client.fallback.UserFeignClientFallback;
import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service", fallbackFactory = UserFeignClientFallback.class)
public interface UserFeignClient {

    @GetMapping("/user/stats")
    Result getUserStats();
}
