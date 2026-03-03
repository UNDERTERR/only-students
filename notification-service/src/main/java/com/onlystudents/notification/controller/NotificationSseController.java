package com.onlystudents.notification.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.notification.sse.SseEmitterManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 实时通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/notification/sse")
@RequiredArgsConstructor
@Tag(name = "SSE实时通知", description = "Server-Sent Events 实时通知推送接口")
public class NotificationSseController {
    
    private final SseEmitterManager sseEmitterManager;
    
    /**
     * 订阅 SSE 实时通知
     * 用户登录后调用此接口建立 SSE 连接
     */
    @GetMapping(value = "/subscribe", produces = "text/event-stream;charset=UTF-8")
    @Operation(summary = "订阅通知", description = "建立SSE连接，接收实时通知推送。需要在URL参数中携带用户ID")
    public SseEmitter subscribe(@RequestParam("userId") Long userId) {
        log.info("用户 {} 订阅SSE通知", userId);
        return sseEmitterManager.createEmitter(userId);
    }
    
    /**
     * 取消订阅（可选，客户端关闭连接时自动处理）
     */
    @PostMapping("/unsubscribe")
    @Operation(summary = "取消订阅", description = "主动取消SSE订阅")
    public void unsubscribe(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        log.info("用户 {} 取消SSE订阅", userId);
        sseEmitterManager.removeEmitter(userId);
    }
    
    /**
     * 获取在线用户数量（管理员接口）
     */
    @GetMapping("/online-count")
    @Operation(summary = "在线用户数量", description = "获取当前SSE连接的在线用户数量")
    public int getOnlineCount() {
        return sseEmitterManager.getOnlineUserCount();
    }
}
