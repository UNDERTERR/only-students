package com.onlystudents.notification.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.notification.entity.Notification;
import com.onlystudents.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "发送通知、获取通知列表、标记已读等接口")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "向指定用户发送系统通知")
    public Result<Notification> sendNotification(@RequestParam(name = "userId") Long userId,
                                               @RequestParam(name = "type") Integer type,
                                               @RequestParam(name = "title") String title,
                                               @RequestParam(name = "content") String content,
                                               @RequestParam(name = "redirectUrl", required = false) String redirectUrl,
                                               @RequestParam(name = "sourceId", required = false) Long sourceId,
                                               @RequestParam(name = "sourceType", required = false) Integer sourceType) {
        return Result.success(notificationService.sendNotification(userId, type, title, content, redirectUrl, sourceId, sourceType));
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表，可按状态筛选")
    public Result<List<Notification>> getNotificationList(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                                         @RequestParam(name = "status", required = false) Integer status,
                                                         @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                         @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(notificationService.getNotificationList(userId, status, page, size));
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数", description = "获取当前用户的未读通知数量")
    public Result<Long> getUnreadCount(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }
    
    @PostMapping("/read/{notificationId}")
    @Operation(summary = "标记通知已读", description = "将指定通知标记为已读状态")
    public Result<Void> markAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                  @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId, userId);
        return Result.success();
    }
    
    @PostMapping("/read-all")
    @Operation(summary = "标记所有已读", description = "将所有通知标记为已读状态")
    public Result<Void> markAllAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
    
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的通知")
    public Result<Void> deleteNotification(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                          @PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId, userId);
        return Result.success();
    }
}
