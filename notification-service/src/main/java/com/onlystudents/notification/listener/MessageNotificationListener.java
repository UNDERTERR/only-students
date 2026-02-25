package com.onlystudents.notification.listener;

import com.onlystudents.common.event.notification.MessageNotificationEvent;
import com.onlystudents.notification.mapper.MessageNotificationMapper;
import com.onlystudents.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageNotificationListener {
    
    private final MessageNotificationMapper messageNotificationMapper;
    private final SseEmitterManager sseEmitterManager;
    
    @RabbitListener(queues = "message.notify.queue")
    @Transactional
    public void handleMessageNotification(MessageNotificationEvent event) {
        log.info("收到私信通知事件: {}", event);
        
        if (event == null || event.getFromUserId() == null || event.getToUserId() == null) {
            return;
        }
        
        if (event.getFromUserId().equals(event.getToUserId())) {
            return;
        }
        
        try {
            messageNotificationMapper.insertMessageNotification(
                event.getFromUserId(),
                event.getToUserId(),
                event.getConversationId(),
                event.getMessageId()
            );
            
            Long unreadCount = messageNotificationMapper.countUnreadByUserId(event.getToUserId());
            sseEmitterManager.sendUnreadCount(event.getToUserId(), unreadCount);
            
            log.info("私信通知创建成功: fromUserId={}, toUserId={}", event.getFromUserId(), event.getToUserId());
        } catch (Exception e) {
            log.error("创建私信通知失败", e);
        }
    }
}
