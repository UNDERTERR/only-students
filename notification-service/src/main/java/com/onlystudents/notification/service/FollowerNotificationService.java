package com.onlystudents.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlystudents.notification.dto.FollowerNotificationDTO;

public interface FollowerNotificationService {
    
    IPage<FollowerNotificationDTO> getNotifications(Long userId, Integer page, Integer size);
    
    Long getUnreadCount(Long userId);
    
    void markAsRead(Long notificationId, Long userId);
    
    void markAllAsRead(Long userId);
    
    void deleteNotification(Long notificationId, Long userId);
}
