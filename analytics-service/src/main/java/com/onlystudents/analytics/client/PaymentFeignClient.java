package com.onlystudents.analytics.client;

import com.onlystudents.analytics.client.fallback.PaymentFeignClientFallback;
import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "payment-service", fallback = PaymentFeignClientFallback.class)
public interface PaymentFeignClient {

    @GetMapping("/payment/creator/{creatorId}/revenue")
    Result<Long> getCreatorRevenue(@PathVariable("creatorId") Long creatorId);
}
