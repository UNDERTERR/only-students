package com.onlystudents.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.notification.entity.Notification;
import com.onlystudents.notification.mapper.NotificationMapper;
import com.onlystudents.notification.service.NotificationService;
import com.onlystudents.notification.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationMapper notificationMapper;
    private final SseEmitterManager sseEmitterManager;
    
    @Override
    @Transactional
    public Notification sendNotification(Long userId, Integer type, String title, String content, 
                                        String redirectUrl, Long sourceId, Integer sourceType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        // redirectUrl、sourceId、sourceType 字段暂不使用，保留在extraData中
        notification.setExtraData("{}");
        notification.setTargetId(sourceId);
        notification.setTargetType(sourceType);
        notification.setIsRead(0);
        notification.setSendChannel(1);
        
        notificationMapper.insert(notification);
        
        // 如果用户在线，通过SSE实时推送
        if (sseEmitterManager.isUserOnline(userId)) {
            sseEmitterManager.sendNotification(userId, notification);
            // 发送未读数更新
            Long unreadCount = notificationMapper.countUnreadByUserId(userId);
            sseEmitterManager.sendUnreadCount(userId, unreadCount);
            log.info("通知已通过SSE推送给用户{}: {}", userId, title);
        } else {
            log.info("通知已存储，用户{}不在线，待登录后查看: {}", userId, title);
        }
        
        return notification;
    }
    
    @Override
    public List<Notification> getNotificationList(Long userId, Integer isRead, Integer page, Integer size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);
        
        Page<Notification> pageParam = new Page<>(page, size);
        return notificationMapper.selectPage(pageParam, wrapper).getRecords();
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该通知");
        }
        
        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        notificationMapper.updateById(notification);
        
        // 发送未读数更新
        Long unreadCount = notificationMapper.countUnreadByUserId(userId);
        sseEmitterManager.sendUnreadCount(userId, unreadCount);
        
        log.info("用户{}标记通知{}为已读", userId, notificationId);
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
        
        // 发送未读数更新
        sseEmitterManager.sendUnreadCount(userId, 0L);
        
        log.info("用户{}标记所有通知为已读", userId);
    }
    
    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除该通知");
        }
        
        notificationMapper.deleteById(notificationId);
        log.info("用户{}删除通知{}", userId, notificationId);
    }
}
