package com.onlystudents.analytics.client.fallback;

import com.onlystudents.analytics.client.PaymentFeignClient;
import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentFeignClientFallback implements PaymentFeignClient {

    @Override
    public Result<Long> getCreatorRevenue(Long creatorId) {
        log.warn("PaymentFeignClient 降级处理: getCreatorRevenue, creatorId={}", creatorId);
        Result<Long> result = new Result<>();
        result.setData(0L);
        return result;
    }
}
