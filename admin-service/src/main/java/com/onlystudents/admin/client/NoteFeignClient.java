package com.onlystudents.admin.client;

import com.onlystudents.admin.client.fallback.NoteFeignClientFallback;
import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "note-service", fallbackFactory = NoteFeignClientFallback.class)
public interface NoteFeignClient {

    @GetMapping("/note/stats")
    Result getNoteStats();
}
