package com.onlystudents.message.client;

import com.onlystudents.common.result.Result;
import com.onlystudents.message.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * 获取用户信息
     */
    @GetMapping("/user/{userId}")
    Result<UserInfoDTO> getUserById(@PathVariable("userId") Long userId);
}
