package com.onlystudents.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.common.result.Result;
import com.onlystudents.notification.client.UserFeignClient;
import com.onlystudents.notification.dto.FollowerNotificationDTO;
import com.onlystudents.notification.entity.FollowerNotification;
import com.onlystudents.notification.mapper.FollowerNotificationMapper;
import com.onlystudents.notification.service.FollowerNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowerNotificationServiceImpl implements FollowerNotificationService {
    
    private final FollowerNotificationMapper followerNotificationMapper;
    private final UserFeignClient userFeignClient;
    
    @Override
    public IPage<FollowerNotificationDTO> getNotifications(Long userId, Integer page, Integer size) {
        Page<FollowerNotification> pageParam = new Page<>(page, size);
        IPage<FollowerNotification> notificationPage = followerNotificationMapper.selectByUserIdPage(pageParam, userId);
        
        return notificationPage.convert(this::convertToDTO);
    }
    
    private FollowerNotificationDTO convertToDTO(FollowerNotification notification) {
        FollowerNotificationDTO dto = new FollowerNotificationDTO();
        dto.setId(notification.getId());
        dto.setFromUserId(notification.getFromUserId());
        dto.setToUserId(notification.getToUserId());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        
        // 获取用户信息
        try {
            Result<Map<String, Object>> userResult = userFeignClient.getUserById(notification.getFromUserId());
            if (userResult != null && userResult.getData() != null) {
                Map<String, Object> userData = userResult.getData();
                dto.setFromUserNickname((String) userData.get("nickname"));
                dto.setFromUserAvatar((String) userData.get("avatar"));
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: fromUserId={}", notification.getFromUserId(), e);
        }
        
        return dto;
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return followerNotificationMapper.countUnreadByUserId(userId);
    }
    
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        FollowerNotification notification = followerNotificationMapper.selectById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            notification.setIsRead(1);
            followerNotificationMapper.updateById(notification);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<FollowerNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowerNotification::getToUserId, userId)
               .eq(FollowerNotification::getIsRead, 0);
        
        FollowerNotification update = new FollowerNotification();
        update.setIsRead(1);
        followerNotificationMapper.update(update, wrapper);
    }
    
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        FollowerNotification notification = followerNotificationMapper.selectById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            followerNotificationMapper.deleteById(notificationId);
        }
    }
}
