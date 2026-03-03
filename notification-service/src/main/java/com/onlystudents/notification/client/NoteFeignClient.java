package com.onlystudents.notification.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Note-Service 客户端
 */
@FeignClient(name = "note-service", url = "${note-service.url:http://localhost:8003}")
public interface NoteFeignClient {
    
    @GetMapping("/note/{id}")
    Result<Map<String, Object>> getNoteById(@PathVariable("id") Long id);
    
    @GetMapping(value = "/note/batch", consumes = "application/json")
    Result<List<Map<String, Object>>> getNotesByIds(@RequestParam("ids") String ids);
}
