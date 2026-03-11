package com.onlystudents.analytics.client.fallback;

import com.onlystudents.analytics.client.NoteFeignClient;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class NoteFeignClientFallback implements NoteFeignClient {

    @Override
    public Result<Map<String, Object>> getCreatorNoteStats(Long creatorId) {
        log.warn("NoteFeignClient 降级处理: getCreatorNoteStats, creatorId={}", creatorId);
        Result<Map<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());
        return result;
    }
}
