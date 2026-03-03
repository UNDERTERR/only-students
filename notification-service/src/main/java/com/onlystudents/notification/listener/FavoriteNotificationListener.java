package com.onlystudents.notification.listener;

import com.onlystudents.common.event.notification.FavoriteNotificationEvent;
import com.onlystudents.notification.mapper.FavoriteNotificationMapper;
import com.onlystudents.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FavoriteNotificationListener {
    
    private final FavoriteNotificationMapper favoriteNotificationMapper;
    private final SseEmitterManager sseEmitterManager;
    
    @RabbitListener(queues = "favorite.notify.queue")
    @Transactional
    public void handleFavoriteNotification(FavoriteNotificationEvent event) {
        log.info("收到收藏通知事件: {}", event);
        
        if (event == null || event.getFromUserId() == null || event.getToUserId() == null) {
            return;
        }
        
        // 不给自己收藏
        if (event.getFromUserId().equals(event.getToUserId())) {
            return;
        }
        
        try {
            // 创建收藏通知记录
            favoriteNotificationMapper.insertFavoriteNotification(
                event.getFromUserId(),
                event.getToUserId(),
                event.getNoteId(),
                event.getFavoriteId()
            );
            
            // 发送未读计数更新
            Long unreadCount = favoriteNotificationMapper.countUnreadByUserId(event.getToUserId());
            sseEmitterManager.sendUnreadCount(event.getToUserId(), unreadCount);
            
            log.info("收藏通知创建成功: fromUserId={}, toUserId={}", event.getFromUserId(), event.getToUserId());
        } catch (Exception e) {
            log.error("创建收藏通知失败", e);
        }
    }
}
