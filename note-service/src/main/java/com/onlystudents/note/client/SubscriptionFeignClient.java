package com.onlystudents.note.client;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.note.client.fallback.SubscriptionFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 订阅服务 Feign 客户端
 * 用于 note-service 调用 subscription-service 检查订阅状态
 */
@FeignClient(
        name = "subscription-service",
        fallback = SubscriptionFeignClientFallback.class
)
public interface SubscriptionFeignClient {

    /**
     * 检查订阅状态
     *
     * @param creatorId 创作者ID
     * @param subscriberId 订阅者ID
     * @return 是否已订阅
     */
    @GetMapping("/subscription/check/{creatorId}")
    Result<Boolean> checkSubscription(@PathVariable("creatorId") Long creatorId,
                                      @RequestHeader(CommonConstants.USER_ID_HEADER) Long subscriberId);
}
