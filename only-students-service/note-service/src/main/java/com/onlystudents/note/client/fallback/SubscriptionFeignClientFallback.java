package com.onlystudents.note.client.fallback;

import com.onlystudents.common.core.result.Result;
import com.onlystudents.note.client.SubscriptionFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订阅服务 Feign 客户端降级处理类
 * 当 subscription-service 不可用时，默认返回未订阅
 */
@Slf4j
@Component
public class SubscriptionFeignClientFallback implements SubscriptionFeignClient {

    @Override
    public Result<Boolean> checkSubscription(Long creatorId, Long subscriberId) {
        log.error("调用 subscription-service 失败，执行降级处理，creatorId={}, subscriberId={}", creatorId, subscriberId);
        
        // 降级处理：服务不可用时，默认返回未订阅（安全策略）
        return Result.success(false);
    }
}
