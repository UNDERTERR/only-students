package com.onlystudents.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.notification.dto.FollowerNotificationDTO;
import com.onlystudents.notification.service.FollowerNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification/follower")
@RequiredArgsConstructor
@Tag(name = "粉丝通知管理", description = "获取粉丝通知列表、标记已读、删除等接口")
public class FollowerNotificationController {
    
    private final FollowerNotificationService followerNotificationService;
    
    @GetMapping("/list")
    @Operation(summary = "获取粉丝通知列表", description = "获取当前用户的粉丝通知列表，支持分页")
    public Result<IPage<FollowerNotificationDTO>> getNotificationList(
            @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(followerNotificationService.getNotifications(userId, page, size));
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数", description = "获取当前用户的未读粉丝通知数量")
    public Result<Long> getUnreadCount(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(followerNotificationService.getUnreadCount(userId));
    }
    
    @PostMapping("/read/{notificationId}")
    @Operation(summary = "标记通知已读", description = "将指定粉丝通知标记为已读状态")
    public Result<Void> markAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                   @PathVariable Long notificationId) {
        followerNotificationService.markAsRead(notificationId, userId);
        return Result.success();
    }
    
    @PostMapping("/read-all")
    @Operation(summary = "标记所有已读", description = "将所有粉丝通知标记为已读状态")
    public Result<Void> markAllAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        followerNotificationService.markAllAsRead(userId);
        return Result.success();
    }
    
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的粉丝通知（物理删除）")
    public Result<Void> deleteNotification(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                           @PathVariable Long notificationId) {
        followerNotificationService.deleteNotification(notificationId, userId);
        return Result.success();
    }
}
