package com.onlystudents.note.client.fallback;

import com.onlystudents.common.core.result.Result;
import com.onlystudents.note.client.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务 Feign 客户端降级处理类
 * 当 user-service 不可用时，返回降级数据
 */
@Slf4j
@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public Result<Map<String, Object>> getUserById(Long userId) {
        log.error("调用 user-service 失败，执行降级处理，userId={}", userId);
        
        // 构建降级用户信息
        Map<String, Object> fallbackUser = new HashMap<>();
        fallbackUser.put("id", userId);
        fallbackUser.put("username", "用户_" + userId);
        fallbackUser.put("nickname", "用户_" + userId);
        fallbackUser.put("avatar", "");
        
        return Result.success(fallbackUser);
    }
}
