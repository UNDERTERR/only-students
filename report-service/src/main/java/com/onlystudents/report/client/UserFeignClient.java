package com.onlystudents.report.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务 Feign 客户端
 * 用于 report-service 调用 user-service 封禁用户
 */
@FeignClient(name = "user-service")
public interface UserFeignClient {

    /**
     * 设置封禁时间（普通举报封禁）
     */
    @PutMapping("/user/{userId}/ban")
    Result<Void> setUserBan(@PathVariable("userId") Long userId,
                           @RequestParam("banTime") java.time.LocalDateTime banTime,
                           @RequestParam("reason") String reason);

    /**
     * 直接冻结用户
     */
    @PutMapping("/user/{userId}/freeze")
    Result<Void> setUserFreeze(@PathVariable("userId") Long userId,
                              @RequestParam("freezeTime") java.time.LocalDateTime freezeTime,
                              @RequestParam("reason") String reason);
}
