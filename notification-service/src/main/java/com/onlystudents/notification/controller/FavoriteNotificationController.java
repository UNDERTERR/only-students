package com.onlystudents.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.notification.dto.FavoriteNotificationDTO;
import com.onlystudents.notification.service.FavoriteNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification/favorite")
@RequiredArgsConstructor
@Tag(name = "收藏通知管理", description = "获取收藏通知列表、标记已读、删除等接口")
public class FavoriteNotificationController {
    
    private final FavoriteNotificationService favoriteNotificationService;
    
    @GetMapping("/list")
    @Operation(summary = "获取收藏通知列表", description = "获取当前用户的收藏通知列表，支持分页")
    public Result<IPage<FavoriteNotificationDTO>> getNotificationList(
            @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return Result.success(favoriteNotificationService.getNotifications(userId, page, size));
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数", description = "获取当前用户的未读收藏通知数量")
    public Result<Long> getUnreadCount(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(favoriteNotificationService.getUnreadCount(userId));
    }
    
    @PostMapping("/read/{notificationId}")
    @Operation(summary = "标记通知已读", description = "将指定收藏通知标记为已读状态")
    public Result<Void> markAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                   @PathVariable Long notificationId) {
        favoriteNotificationService.markAsRead(notificationId, userId);
        return Result.success();
    }
    
    @PostMapping("/read-all")
    @Operation(summary = "标记所有已读", description = "将所有收藏通知标记为已读状态")
    public Result<Void> markAllAsRead(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        favoriteNotificationService.markAllAsRead(userId);
        return Result.success();
    }
    
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的收藏通知（物理删除）")
    public Result<Void> deleteNotification(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                           @PathVariable Long notificationId) {
        favoriteNotificationService.deleteNotification(notificationId, userId);
        return Result.success();
    }
}
