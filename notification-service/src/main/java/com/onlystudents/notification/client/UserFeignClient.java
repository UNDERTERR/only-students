package com.onlystudents.notification.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * User-Service 客户端
 */
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8001}")
public interface UserFeignClient {
    
    @GetMapping("/user/{id}")
    Result<Map<String, Object>> getUserById(@PathVariable("id") Long id);
    
    @GetMapping("/user/batch/{ids}")
    Result<List<Map<String, Object>>> getUsersByIds(@PathVariable("ids") String ids);
}
