package com.onlystudents.search.client.fallback;

import com.onlystudents.common.result.Result;
import com.onlystudents.search.client.UserClient;
import com.onlystudents.search.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserClientFallback implements UserClient {
    
    @Override
    public Result<List<UserResponse>> searchUsers(String keyword, Integer educationLevel, Integer isCreator, Integer page, Integer size) {
        log.warn("User-Service 调用失败，返回空结果");
        return Result.success(new ArrayList<>());
    }
}
