package com.onlystudents.comment.client;

import com.onlystudents.comment.client.fallback.UserFeignClientFallback;
import com.onlystudents.comment.dto.UserResponse;
import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 * 用于 comment-service 调用 user-service
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
     * @return 用户信息
     */
    @GetMapping("/user/{userId}")
    Result<UserResponse> getUserById(@PathVariable("userId") Long userId);
    
    /**
     * 检查用户是否可以发布内容
     * @param userId 用户ID
     * @return 如果可以发布返回true，否则返回false
     */
    @GetMapping("/user/{userId}/canPost")
    Result<Boolean> canUserPost(@PathVariable("userId") Long userId);
}
