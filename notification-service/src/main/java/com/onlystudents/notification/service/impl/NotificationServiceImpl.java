package com.onlystudents.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.notification.entity.Notification;
import com.onlystudents.notification.mapper.NotificationMapper;
import com.onlystudents.notification.service.NotificationService;
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
    
    @Override
    @Transactional
    public Notification sendNotification(Long userId, Integer type, String title, String content, 
                                       String redirectUrl, Long sourceId, Integer sourceType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRedirectUrl(redirectUrl);
        notification.setSourceId(sourceId);
        notification.setSourceType(sourceType);
        notification.setStatus(0);
        
        notificationMapper.insert(notification);
        
        log.info("发送通知给用户{}: {}", userId, title);
        return notification;
    }
    
    @Override
    public List<Notification> getNotificationList(Long userId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (status != null) {
            wrapper.eq(Notification::getStatus, status);
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
        
        notification.setStatus(1);
        notification.setReadTime(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notificationMapper.updateById(notification);
        
        log.info("用户{}标记通知{}为已读", userId, notificationId);
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
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
