package com.onlystudents.note.client;

import com.onlystudents.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 通知服务 Feign 客户端
 * 用于 note-service 调用 notification-service 发送通知
 */
@FeignClient(name = "notification-service", fallback = NotificationFeignClientFallback.class)
public interface NotificationFeignClient {

    /**
     * 发送系统通知
     * @param userId 接收用户ID
     * @param type 通知类型（1-系统通知）
     * @param title 通知标题
     * @param content 通知内容
     * @return 结果
     */
    @PostMapping("/notification/send")
    Result<Void> sendNotification(
        @RequestParam("userId") Long userId,
        @RequestParam("type") Integer type,
        @RequestParam("title") String title,
        @RequestParam("content") String content
    );
}
