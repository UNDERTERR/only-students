package com.onlystudents.analytics.client;

import com.onlystudents.analytics.client.fallback.RatingFeignClientFallback;
import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "rating-service", fallback = RatingFeignClientFallback.class)
public interface RatingFeignClient {

    @GetMapping("/rating/creator/{creatorId}/stats")
    Result<Map<String, Object>> getCreatorRatingStats(@PathVariable("creatorId") Long creatorId);
}
