package com.onlystudents.notification.listener;

import com.onlystudents.notification.entity.Notification;
import com.onlystudents.notification.event.NoteFavoriteEvent;
import com.onlystudents.notification.mapper.NotificationMapper;
import com.onlystudents.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteFavoriteEventListener {
    
    private final NotificationMapper notificationMapper;
    private final SseEmitterManager sseEmitterManager;
    
    @RabbitListener(queues = "rating.favorite.queue")
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

    // 创建通知
    Notification notification = new Notification();
    notification.setUserId(event.getNoteAuthorId());
    notification.setType(2); // 2-收藏类型
    notification.setTitle("有人收藏了你的笔记");
    notification.setContent("用户收藏了你的笔记: " + (event.getNoteTitle() != null ? event.getNoteTitle() : ""));
    notification.setTargetId(event.getNoteId());
    notification.setTargetType(1); // 1-笔记
    notification.setIsRead(0);
    notification.setSendChannel(1); // 1-系统通知

    notificationMapper.insert(notification);
    log.info("创建收藏通知成功: notificationId={}, userId={}", notification.getId(), notification.getUserId());

    // 发送未读计数更新
    Long unreadCount = notificationMapper.countByUserId(event.getNoteAuthorId());
    sseEmitterManager.sendUnreadCount(event.getNoteAuthorId(), unreadCount);
}
}
