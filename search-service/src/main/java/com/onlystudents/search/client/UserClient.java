package com.onlystudents.search.client;

import com.onlystudents.common.result.Result;
import com.onlystudents.search.client.fallback.UserClientFallback;
import com.onlystudents.search.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", path = "/user", fallback = UserClientFallback.class)
public interface UserClient {
    
    @GetMapping("/search")
    Result<List<UserResponse>> searchUsers(@RequestParam("keyword") String keyword,
                                            @RequestParam(value = "educationLevel", required = false) Integer educationLevel,
                                            @RequestParam(value = "isCreator", required = false) Integer isCreator,
                                            @RequestParam("page") Integer page,
                                            @RequestParam("size") Integer size);

}
