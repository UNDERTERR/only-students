package com.onlystudents.note.client;

import com.onlystudents.common.core.result.Result;
import com.onlystudents.note.client.fallback.UserFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务 Feign 客户端
 * 用于 note-service 调用 user-service 获取用户信息
 */
@FeignClient(
        name = "user-service",
        fallback = UserFeignClientFallback.class
)
public interface UserFeignClient {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息（包含 username, nickname, avatar 等字段）
     */
    @GetMapping("/user/{userId}")
    Result<Map<String, Object>> getUserById(@PathVariable("userId") Long userId);
}
