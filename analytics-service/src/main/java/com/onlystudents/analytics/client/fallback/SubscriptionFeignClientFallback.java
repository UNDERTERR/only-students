package com.onlystudents.analytics.client.fallback;

import com.onlystudents.analytics.client.SubscriptionFeignClient;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubscriptionFeignClientFallback implements SubscriptionFeignClient {

    @Override
    public Result<Integer> getSubscriberCount(Long creatorId) {
        log.warn("SubscriptionFeignClient 降级处理: getSubscriberCount, creatorId={}", creatorId);
        Result<Integer> result = new Result<>();
        result.setData(0);
        return result;
    }
}
