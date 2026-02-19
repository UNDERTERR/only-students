package com.onlystudents.comment.client.fallback;

import com.onlystudents.comment.client.UserFeignClient;
import com.onlystudents.comment.client.UserResponse;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户服务 Feign 客户端降级处理类
 * 当 user-service 不可用时，返回降级数据
 */
@Slf4j
@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public Result<UserResponse> getUserById(Long userId) {
        log.error("调用 user-service 失败，执行降级处理，userId={}", userId);
        
        UserResponse fallbackUser = new UserResponse();
        fallbackUser.setId(userId);
        fallbackUser.setUsername("用户_" + userId);
        fallbackUser.setNickname("用户_" + userId);
        fallbackUser.setAvatar("");
        
        return Result.success(fallbackUser);
    }
}
