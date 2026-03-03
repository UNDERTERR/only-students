package com.onlystudents.notification.listener;

import com.onlystudents.common.event.notification.FollowerNotificationEvent;
import com.onlystudents.notification.mapper.FollowerNotificationMapper;
import com.onlystudents.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowerNotificationListener {
    
    private final FollowerNotificationMapper followerNotificationMapper;
    private final SseEmitterManager sseEmitterManager;
    
    @RabbitListener(queues = "follower.notify.queue")
    @Transactional
    public void handleFollowerNotification(FollowerNotificationEvent event) {
        log.info("收到粉丝通知事件: {}", event);
        
        if (event == null || event.getFromUserId() == null || event.getToUserId() == null) {
            return;
        }
        
        if (event.getFromUserId().equals(event.getToUserId())) {
            return;
        }
        
        try {
            followerNotificationMapper.insertFollowerNotification(
                event.getFromUserId(),
                event.getToUserId(),
                event.getSubscriptionId()
            );
            
            Long unreadCount = followerNotificationMapper.countUnreadByUserId(event.getToUserId());
            sseEmitterManager.sendUnreadCount(event.getToUserId(), unreadCount);
            
            log.info("粉丝通知创建成功: fromUserId={}, toUserId={}", event.getFromUserId(), event.getToUserId());
        } catch (Exception e) {
            log.error("创建粉丝通知失败", e);
        }
    }
}
