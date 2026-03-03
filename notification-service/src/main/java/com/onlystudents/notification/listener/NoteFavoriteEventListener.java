package com.onlystudents.notification.listener;

import com.onlystudents.common.event.note.NoteFavoriteEvent;
import com.onlystudents.notification.mapper.NotificationMapper;
import com.onlystudents.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteFavoriteEventListener {
    
    private final NotificationMapper notificationMapper;
    private final SseEmitterManager sseEmitterManager;
    
    @RabbitListener(queues = "rating.favorite.queue")
    @Transactional
    public void handleNoteFavoriteEvent(NoteFavoriteEvent event) {
    log.info("收到笔记收藏事件: {}", event);

    // 只处理收藏事件（action=1），不处理取消收藏
    if (event == null || event.getAction() == 0) {
        return;
    }

    // 不给自己收藏
    if (event.getUserId().equals(event.getNoteAuthorId())) {
        return;
    }

    // 收藏已有专门模块，不再插入系统通知
    // 只发送未读计数更新
    Long unreadCount = notificationMapper.countByUserId(event.getNoteAuthorId());
    sseEmitterManager.sendUnreadCount(event.getNoteAuthorId(), unreadCount);
}
}
