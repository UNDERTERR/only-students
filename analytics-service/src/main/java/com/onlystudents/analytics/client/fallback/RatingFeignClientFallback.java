package com.onlystudents.analytics.client.fallback;

import com.onlystudents.analytics.client.RatingFeignClient;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RatingFeignClientFallback implements RatingFeignClient {

    @Override
    public Result<Map<String, Object>> getCreatorRatingStats(Long creatorId) {
        log.warn("RatingFeignClient 降级处理: getCreatorRatingStats, creatorId={}", creatorId);
        Result<Map<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());
        return result;
    }
}
