package com.onlystudents.note.client;

import com.onlystudents.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通知服务 Feign 客户端降级处理
 */
@Slf4j
@Component
public class NotificationFeignClientFallback implements NotificationFeignClient {

    @Override
    public Result<Void> sendNotification(Long userId, Integer type, String title, String content) {
        log.warn("调用 notification-service 失败，降级处理: userId={}, title={}", userId, title);
        return Result.success(null);
    }
}
