package com.onlystudents.notification.sse;

import com.onlystudents.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 连接管理器
 * 管理所有用户的 SSE 连接
 */
@Slf4j
@Component
public class SseEmitterManager {
    
    // 存储用户ID到SseEmitter的映射
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    // 默认超时时间：30分钟
    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L;
    
    /**
     * 创建并注册用户的 SSE 连接
     */
    public SseEmitter createEmitter(Long userId) {
        // 如果用户已有连接，先移除旧连接
        removeEmitter(userId);
        
        // 创建新的 SseEmitter
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        
        // 存储连接
        emitters.put(userId, emitter);
        log.info("用户 {} 建立SSE连接，当前在线用户数: {}", userId, emitters.size());
        
        // 连接完成时移除
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("用户 {} SSE连接完成，当前在线用户数: {}", userId, emitters.size());
        });
        
        // 连接超时时移除
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("用户 {} SSE连接超时，当前在线用户数: {}", userId, emitters.size());
        });
        
        // 发生错误时移除
        emitter.onError((e) -> {
            emitters.remove(userId);
            log.error("用户 {} SSE连接发生错误", userId, e);
        });
        
        // 发送连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("userId", userId, "message", "SSE连接成功")));
        } catch (Exception e) {
            log.warn("发送SSE连接成功事件失败，可能客户端已断开: {}", e.getMessage());
        }
        
        return emitter;
    }
    
    /**
     * 移除用户的 SSE 连接
     */
    public void removeEmitter(Long userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("关闭SSE连接时发生异常", e);
            }
        }
    }
    
    /**
     * 发送通知给指定用户
     */
    public void sendNotification(Long userId, Notification notification) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
                log.debug("通知已通过SSE推送给用户 {}: {}", userId, notification.getTitle());
            } catch (IOException e) {
                log.error("发送SSE通知失败，用户: {}", userId, e);
                // 发送失败，移除连接
                removeEmitter(userId);
            }
        } else {
            log.debug("用户 {} 不在线，通知无法推送: {}", userId, notification.getTitle());
        }
    }
    
    /**
     * 发送未读计数更新给指定用户
     */
    public void sendUnreadCount(Long userId, Long count) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(Map.of("count", count)));
                log.debug("未读计数已通过SSE推送给用户 {}: {}", userId, count);
            } catch (IOException e) {
                log.error("发送SSE未读计数失败，用户: {}", userId, e);
                removeEmitter(userId);
            }
        }
    }
    
    /**
     * 广播通知给所有在线用户
     */
    public void broadcastNotification(Notification notification) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                log.error("广播通知失败，用户: {}", userId, e);
                removeEmitter(userId);
            }
        });
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return emitters.containsKey(userId);
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return emitters.size();
    }
}
