package com.onlystudents.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.common.result.Result;
import com.onlystudents.notification.client.NoteFeignClient;
import com.onlystudents.notification.client.UserFeignClient;
import com.onlystudents.notification.dto.FavoriteNotificationDTO;
import com.onlystudents.notification.entity.FavoriteNotification;
import com.onlystudents.notification.mapper.FavoriteNotificationMapper;
import com.onlystudents.notification.service.FavoriteNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteNotificationServiceImpl implements FavoriteNotificationService {
    
    private final FavoriteNotificationMapper favoriteNotificationMapper;
    private final UserFeignClient userFeignClient;
    private final NoteFeignClient noteFeignClient;
    
    @Override
    public IPage<FavoriteNotificationDTO> getNotifications(Long userId, Integer page, Integer size) {
        Page<FavoriteNotification> pageParam = new Page<>(page, size);
        IPage<FavoriteNotification> notificationPage = favoriteNotificationMapper.selectByUserIdPage(pageParam, userId);
        
        return notificationPage.convert(this::convertToDTO);
    }
    
    private FavoriteNotificationDTO convertToDTO(FavoriteNotification notification) {
        FavoriteNotificationDTO dto = new FavoriteNotificationDTO();
        dto.setId(notification.getId());
        dto.setFromUserId(notification.getFromUserId());
        dto.setToUserId(notification.getToUserId());
        dto.setNoteId(notification.getNoteId());
        dto.setFavoriteId(notification.getFavoriteId());
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
        
        // 获取笔记信息
        try {
            Result<Map<String, Object>> noteResult = noteFeignClient.getNoteById(notification.getNoteId());
            if (noteResult != null && noteResult.getData() != null) {
                Map<String, Object> noteData = noteResult.getData();
                dto.setNoteTitle((String) noteData.get("title"));
                dto.setNoteCoverImage((String) noteData.get("coverImage"));
            }
        } catch (Exception e) {
            log.error("获取笔记信息失败: noteId={}", notification.getNoteId(), e);
        }
        
        return dto;
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return favoriteNotificationMapper.countUnreadByUserId(userId);
    }
    
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        FavoriteNotification notification = favoriteNotificationMapper.selectById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            notification.setIsRead(1);
            favoriteNotificationMapper.updateById(notification);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<FavoriteNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteNotification::getToUserId, userId)
               .eq(FavoriteNotification::getIsRead, 0);
        
        FavoriteNotification update = new FavoriteNotification();
        update.setIsRead(1);
        favoriteNotificationMapper.update(update, wrapper);
    }
    
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        FavoriteNotification notification = favoriteNotificationMapper.selectById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            favoriteNotificationMapper.deleteById(notificationId);
        }
    }
}
