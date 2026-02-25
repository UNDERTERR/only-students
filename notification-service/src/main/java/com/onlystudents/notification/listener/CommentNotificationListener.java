package com.onlystudents.notification.listener;

import com.onlystudents.common.event.notification.CommentNotificationEvent;
import com.onlystudents.notification.mapper.CommentNotificationMapper;
import com.onlystudents.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationListener {
    
    private final CommentNotificationMapper commentNotificationMapper;
    private final SseEmitterManager sseEmitterManager;
    
    @RabbitListener(queues = "comment.notify.queue")
    @Transactional
    public void handleCommentNotification(CommentNotificationEvent event) {
        log.info("收到评论通知事件: {}", event);
        
        if (event == null || event.getFromUserId() == null || event.getToUserId() == null) {
            return;
        }
        
        if (event.getFromUserId().equals(event.getToUserId())) {
            return;
        }
        
        try {
            commentNotificationMapper.insertCommentNotification(
                event.getFromUserId(),
                event.getToUserId(),
                event.getNoteId(),
                event.getCommentId()
            );
            
            Long unreadCount = commentNotificationMapper.countUnreadByUserId(event.getToUserId());
            sseEmitterManager.sendUnreadCount(event.getToUserId(), unreadCount);
            
            log.info("评论通知创建成功: fromUserId={}, toUserId={}", event.getFromUserId(), event.getToUserId());
        } catch (Exception e) {
            log.error("创建评论通知失败", e);
        }
    }
}
