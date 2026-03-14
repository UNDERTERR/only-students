package com.onlystudents.note.client;

import com.onlystudents.common.result.Result;
import com.onlystudents.note.client.fallback.UserFeignClientFallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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
     * @return 用户信息（包含 nickname, avatar 等字段）
     */
    @GetMapping("/user/{userId}")
    Result<Map<String, Object>> getUserById(@PathVariable("userId") Long userId);

    /**
     * 增加学校笔记数
     */
    @PostMapping("/user/school/notes/increment/{schoolId}")
    Result<Void> incrementSchoolNotes(@PathVariable("schoolId") Long schoolId);

    /**
     * 减少学校笔记数
     */
    @PostMapping("/user/school/notes/decrement/{schoolId}")
    Result<Void> decrementSchoolNotes(@PathVariable("schoolId") Long schoolId);
    
    /**
     * 检查用户是否可以发布内容
     * @param userId 用户ID
     * @return 如果可以发布返回true，否则返回false
     */
    @GetMapping("/user/{userId}/canPost")
    Result<Boolean> canUserPost(@PathVariable("userId") Long userId);
}


