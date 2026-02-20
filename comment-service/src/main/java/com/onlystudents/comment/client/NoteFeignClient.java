package com.onlystudents.comment.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * Note-Service 客户端
 */
@FeignClient(name = "note-service", url = "${note-service.url:http://localhost:8003}", fallbackFactory = NoteFeignClientFallbackFactory.class)
public interface NoteFeignClient {
    
    @GetMapping("/note/{id}")
    Result<Map<String, Object>> getNoteById(@PathVariable("id") Long id);
    
    @GetMapping("/note/ids/user/{userId}")
    Result<List<Long>> getNoteIdsByUserId(@PathVariable("userId") Long userId);
}
