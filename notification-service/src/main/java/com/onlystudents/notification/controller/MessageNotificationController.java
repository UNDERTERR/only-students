package com.onlystudents.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.notification.dto.MessageNotificationDTO;
import com.onlystudents.notification.service.MessageNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification/message")
@RequiredArgsConstructor
@Tag(name = "私信通知管理", description = "获取私信通知列表、标记已读、删除等接口")
public class MessageNotificationController {
    
    private final MessageNotificationService messageNotificationService;
    
    @GetMapping("/list")
    @Operation(summary = "获取私信通知列表", description = "获取当前用户的私信通知列表，支持分页")
    public Result<IPage<MessageNotificationDTO>> getNotificationList(
            @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(messageNotificationService.getNotifications(userId, page, size));
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数", description = "获取当前用户的未读私信通知数量")
    public Result<Long> getUnreadCount(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(messageNotificationService.getUnreadCount(userId));
    }
    
    @PostMapping("/read/{notificationId}")
    @Operation(summary = "标记通知已读", description = "将指定私信通知标记为已读状态")
    public Result<Void> markAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                   @PathVariable Long notificationId) {
        messageNotificationService.markAsRead(notificationId, userId);
        return Result.success();
    }
    
    @PostMapping("/read-all")
    @Operation(summary = "标记所有已读", description = "将所有私信通知标记为已读状态")
    public Result<Void> markAllAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        messageNotificationService.markAllAsRead(userId);
        return Result.success();
    }
    
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的私信通知（逻辑删除）")
    public Result<Void> deleteNotification(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                           @PathVariable Long notificationId) {
        messageNotificationService.deleteNotification(notificationId, userId);
        return Result.success();
    }
}
