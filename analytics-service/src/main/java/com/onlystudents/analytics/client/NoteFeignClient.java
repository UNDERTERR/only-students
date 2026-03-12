package com.onlystudents.analytics.client;

import com.onlystudents.analytics.client.fallback.NoteFeignClientFallback;
import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "note-service", fallback = NoteFeignClientFallback.class)
public interface NoteFeignClient {

    @GetMapping("/note/creator/{creatorId}/stats")
    Result<Map<String, Object>> getCreatorNoteStats(@PathVariable("creatorId") Long creatorId);
}
