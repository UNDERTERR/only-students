package com.onlystudents.notification.listener;

import com.onlystudents.notification.entity.Notification;
import com.onlystudents.common.event.note.NotePublishEvent;
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
public class NotePublishEventListener {
    
    private final NotificationMapper notificationMapper;
    private final SseEmitterManager sseEmitterManager;
    
    @RabbitListener(queues = "note.publish.queue")
    @Transactional
    public void handleNotePublishEvent(NotePublishEvent event) {
        log.info("收到笔记发布事件: {}", event);
        
        if (event == null) {
            return;
        }
        
        // 创建通知 - 通知作者发布成功
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(1); // 1-系统类型
        notification.setTitle("笔记发布成功");
        notification.setContent("您的笔记《" + (event.getTitle() != null ? event.getTitle() : "") + "》已发布成功！");
        notification.setTargetId(event.getNoteId());
        notification.setTargetType(1); // 1-笔记
        notification.setIsRead(0);
        notification.setSendChannel(1); // 1-系统通知
        
        notificationMapper.insert(notification);
        log.info("创建笔记发布成功通知成功: notificationId={}, userId={}", notification.getId(), notification.getUserId());
        
        // 发送未读计数更新
        Long unreadCount = notificationMapper.countByUserId(event.getUserId());
        sseEmitterManager.sendUnreadCount(event.getUserId(), unreadCount);
    }
}
