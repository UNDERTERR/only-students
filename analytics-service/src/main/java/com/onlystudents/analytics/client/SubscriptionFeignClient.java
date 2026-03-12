package com.onlystudents.analytics.client;

import com.onlystudents.analytics.client.fallback.SubscriptionFeignClientFallback;
import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "subscription-service", fallback = SubscriptionFeignClientFallback.class)
public interface SubscriptionFeignClient {

    @GetMapping("/subscription/subscriber-count/{creatorId}")
    Result<Integer> getSubscriberCount(@PathVariable("creatorId") Long creatorId);
}
