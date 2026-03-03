package com.onlystudents.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.notification.dto.CommentNotificationDTO;
import com.onlystudents.notification.service.CommentNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification/comment")
@RequiredArgsConstructor
@Tag(name = "评论通知管理", description = "获取评论通知列表、标记已读、删除等接口")
public class CommentNotificationController {
    
    private final CommentNotificationService commentNotificationService;
    
    @GetMapping("/list")
    @Operation(summary = "获取评论通知列表", description = "获取当前用户的评论通知列表，支持分页")
    public Result<IPage<CommentNotificationDTO>> getNotificationList(
            @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(commentNotificationService.getNotifications(userId, page, size));
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数", description = "获取当前用户的未读评论通知数量")
    public Result<Long> getUnreadCount(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(commentNotificationService.getUnreadCount(userId));
    }
    
    @PostMapping("/read/{notificationId}")
    @Operation(summary = "标记通知已读", description = "将指定评论通知标记为已读状态")
    public Result<Void> markAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                   @PathVariable Long notificationId) {
        commentNotificationService.markAsRead(notificationId, userId);
        return Result.success();
    }
    
    @PostMapping("/read-all")
    @Operation(summary = "标记所有已读", description = "将所有评论通知标记为已读状态")
    public Result<Void> markAllAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        commentNotificationService.markAllAsRead(userId);
        return Result.success();
    }
    
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的评论通知（物理删除）")
    public Result<Void> deleteNotification(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                           @PathVariable Long notificationId) {
        commentNotificationService.deleteNotification(notificationId, userId);
        return Result.success();
    }
}
