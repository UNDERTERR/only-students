package com.onlystudents.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlystudents.common.result.Result;
import com.onlystudents.notification.client.NoteFeignClient;
import com.onlystudents.notification.client.UserFeignClient;
import com.onlystudents.notification.dto.CommentNotificationDTO;
import com.onlystudents.notification.entity.CommentNotification;
import com.onlystudents.notification.mapper.CommentNotificationMapper;
import com.onlystudents.notification.service.CommentNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentNotificationServiceImpl implements CommentNotificationService {
    
    private final CommentNotificationMapper commentNotificationMapper;
    private final UserFeignClient userFeignClient;
    private final NoteFeignClient noteFeignClient;
    
    @Override
    public IPage<CommentNotificationDTO> getNotifications(Long userId, Integer page, Integer size) {
        Page<CommentNotification> pageParam = new Page<>(page, size);
        IPage<CommentNotification> notificationPage = commentNotificationMapper.selectByUserIdPage(pageParam, userId);
        
        return notificationPage.convert(this::convertToDTO);
    }
    
    private CommentNotificationDTO convertToDTO(CommentNotification notification) {
        CommentNotificationDTO dto = new CommentNotificationDTO();
        dto.setId(notification.getId());
        dto.setFromUserId(notification.getFromUserId());
        dto.setToUserId(notification.getToUserId());
        dto.setNoteId(notification.getNoteId());
        dto.setCommentId(notification.getCommentId());
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
        return commentNotificationMapper.countUnreadByUserId(userId);
    }
    
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        CommentNotification notification = commentNotificationMapper.selectById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            notification.setIsRead(1);
            commentNotificationMapper.updateById(notification);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<CommentNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentNotification::getToUserId, userId)
               .eq(CommentNotification::getIsRead, 0);
        
        CommentNotification update = new CommentNotification();
        update.setIsRead(1);
        commentNotificationMapper.update(update, wrapper);
    }
    
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        CommentNotification notification = commentNotificationMapper.selectById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            commentNotificationMapper.deleteById(notificationId);
        }
    }
}
