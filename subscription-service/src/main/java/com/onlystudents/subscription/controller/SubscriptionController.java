package com.onlystudents.subscription.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.subscription.dto.*;
import com.onlystudents.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
@Tag(name = "订阅管理", description = "订阅/取消订阅/配置管理等接口")
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    @PostMapping
    @Operation(summary = "订阅创作者", description = "订阅指定创作者")
    public Result<SubscriptionDTO> subscribe(@RequestBody SubscribeRequest request,
                                              @RequestHeader(CommonConstants.USER_ID_HEADER) Long subscriberId) {
        return Result.success(subscriptionService.subscribe(request, subscriberId));
    }
    
    @DeleteMapping("/{creatorId}")
    @Operation(summary = "取消订阅", description = "取消对创作者的订阅")
    public Result<Void> unsubscribe(@PathVariable Long creatorId,
                                     @RequestHeader(CommonConstants.USER_ID_HEADER) Long subscriberId) {
        subscriptionService.unsubscribe(creatorId, subscriberId);
        return Result.success();
    }
    
    @GetMapping("/check/{creatorId}")
    @Operation(summary = "检查订阅状态", description = "检查当前用户是否订阅了指定创作者")
    public Result<Boolean> checkSubscription(@PathVariable Long creatorId,
                                              @RequestHeader(CommonConstants.USER_ID_HEADER) Long subscriberId) {
        return Result.success(subscriptionService.checkSubscription(subscriberId, creatorId));
    }
    
    @GetMapping("/my-subscriptions")
    @Operation(summary = "我的订阅", description = "获取我订阅的所有创作者")
    public Result<List<SubscriptionDTO>> getMySubscriptions(@RequestHeader(CommonConstants.USER_ID_HEADER) Long subscriberId) {
        return Result.success(subscriptionService.getMySubscriptions(subscriberId));
    }
    
    @GetMapping("/my-subscribers")
    @Operation(summary = "我的粉丝", description = "获取订阅我的所有用户")
    public Result<List<SubscriptionDTO>> getMySubscribers(@RequestHeader(CommonConstants.USER_ID_HEADER) Long creatorId) {
        return Result.success(subscriptionService.getMySubscribers(creatorId));
    }
    
    @GetMapping("/subscriber-count/{creatorId}")
    @Operation(summary = "粉丝数", description = "获取创作者的粉丝数量")
    public Result<Integer> getSubscriberCount(@PathVariable Long creatorId) {
        return Result.success(subscriptionService.getSubscriberCount(creatorId));
    }
    
    @GetMapping("/config/{creatorId}")
    @Operation(summary = "获取订阅配置", description = "获取创作者的订阅配置")
    public Result<CreatorConfigDTO> getCreatorConfig(@PathVariable Long creatorId) {
        return Result.success(subscriptionService.getCreatorConfig(creatorId));
    }
    
    @PutMapping("/config")
    @Operation(summary = "更新订阅配置", description = "更新我的订阅配置")
    public Result<CreatorConfigDTO> updateCreatorConfig(@RequestBody UpdateCreatorConfigRequest request,
                                                         @RequestHeader(CommonConstants.USER_ID_HEADER) Long creatorId) {
        return Result.success(subscriptionService.updateCreatorConfig(creatorId, request));
    }
}
