package com.onlystudents.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.notification.dto.MessageNotificationDTO;
import com.onlystudents.notification.entity.MessageNotification;
import com.onlystudents.notification.mapper.MessageNotificationMapper;
import com.onlystudents.notification.service.MessageNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageNotificationServiceImpl implements MessageNotificationService {
    
    private final MessageNotificationMapper messageNotificationMapper;
    
    @Override
    public IPage<MessageNotificationDTO> getNotifications(Long userId, Integer page, Integer size) {
        Page<MessageNotification> pageParam = new Page<>(page, size);
        IPage<MessageNotification> notificationPage = messageNotificationMapper.selectByUserIdPage(pageParam, userId);
        
        return notificationPage.convert(this::convertToDTO);
    }
    
    private MessageNotificationDTO convertToDTO(MessageNotification notification) {
        MessageNotificationDTO dto = new MessageNotificationDTO();
        dto.setId(notification.getId());
        dto.setFromUserId(notification.getFromUserId());
        dto.setToUserId(notification.getToUserId());
        dto.setConversationId(notification.getConversationId());
        dto.setMessageId(notification.getMessageId());
        dto.setIsRead(notification.getIsRead());
        dto.setIsDeleted(notification.getIsDeleted());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return messageNotificationMapper.countUnreadByUserId(userId);
    }
    
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        MessageNotification notification = messageNotificationMapper.selectById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            notification.setIsRead(1);
            messageNotificationMapper.updateById(notification);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<MessageNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageNotification::getToUserId, userId)
               .eq(MessageNotification::getIsRead, 0)
               .eq(MessageNotification::getIsDeleted, 0);
        
        MessageNotification update = new MessageNotification();
        update.setIsRead(1);
        messageNotificationMapper.update(update, wrapper);
    }
    
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        MessageNotification notification = messageNotificationMapper.selectById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            notification.setIsDeleted(1);
            messageNotificationMapper.updateById(notification);
        }
    }
}
