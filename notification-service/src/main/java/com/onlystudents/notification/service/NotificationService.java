package com.onlystudents.notification.service;

import com.onlystudents.notification.entity.Notification;

import java.util.List;

public interface NotificationService {
    
    Notification sendNotification(Long userId, Integer type, String title, String content, String redirectUrl, Long sourceId, Integer sourceType);
    
    List<Notification> getNotificationList(Long userId, Integer isRead, Integer page, Integer size);
    
    Long getUnreadCount(Long userId);
    
    void markAsRead(Long notificationId, Long userId);
    
    void markAllAsRead(Long userId);
    
    void deleteNotification(Long notificationId, Long userId);
}
