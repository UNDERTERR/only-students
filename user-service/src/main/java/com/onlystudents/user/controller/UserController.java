package com.onlystudents.user.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.user.dto.*;
import com.onlystudents.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、更新等接口")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，支持学生和创作者")
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }
    
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录，支持多端设备管理")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        request.setIp(getClientIp(httpRequest));
        return Result.success(userService.login(request));
    }
    
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserResponse> getUserInfo(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }
    
    /**
     * 更新用户信息（包含头像）
     * 头像上传流程：
     * 1. 前端先调用 POST /api/file/upload 上传图片
     * 2. 获取返回的 fileUrl
     * 3. 将 fileUrl 放入 request.avatar 字段调用此接口
     */
    @PutMapping
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的基本信息，avatar字段传入文件上传接口返回的URL")
    public Result<UserResponse> updateUser(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                          @RequestBody UpdateUserRequest request) {
        return Result.success(userService.updateUser(userId, request));
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "获取指定用户信息", description = "根据用户ID获取用户公开信息")
    public Result<UserResponse> getUserById(@PathVariable(name = "userId") Long userId) {
        return Result.success(userService.getUserById(userId));
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户（用户名、昵称、简介）")
    public Result<List<UserResponse>> searchUsers(@RequestParam(name = "keyword") String keyword,
                                                   @RequestParam(name = "educationLevel", required = false) Integer educationLevel,
                                                   @RequestParam(name = "isCreator", required = false) Integer isCreator,
                                                   @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                   @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return Result.success(userService.searchUsers(keyword, educationLevel, isCreator, page, size));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "登出当前设备，不传deviceId则登出当前token对应设备")
    public Result<Void> logout(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                               @RequestParam(name = "deviceId", required = false) String deviceId) {
        userService.logout(userId, deviceId);
        return Result.success();
    }
    
    @PostMapping("/logout-all")
    @Operation(summary = "登出所有设备", description = "强制登出用户所有登录设备")
    public Result<Void> logoutAllDevices(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        userService.logoutAllDevices(userId);
        return Result.success();
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    @GetMapping("/batch/{ids}")
    @Operation(summary = "批量获取用户信息", description = "根据ID列表批量获取用户信息")
    public Result<List<UserResponse>> getUsersByIds(@PathVariable(name = "ids") String ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(List.of());
        }
        List<Long> userIds = java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
        return Result.success(userService.getUsersByIds(userIds));
    }
    
    @PostMapping("/follower-count/increment/{userId}")
    @Operation(summary = "增加粉丝数", description = "增加指定用户的粉丝数")
    public Result<Void> incrementFollowerCount(@PathVariable(name = "userId") Long userId) {
        userService.incrementFollowerCount(userId);
        return Result.success();
    }
    
    @PostMapping("/follower-count/decrement/{userId}")
    @Operation(summary = "减少粉丝数", description = "减少指定用户的粉丝数")
    public Result<Void> decrementFollowerCount(@PathVariable(name = "userId") Long userId) {
        userService.decrementFollowerCount(userId);
        return Result.success();
    }
    
    @DeleteMapping("/cache/{userId}")
    @Operation(summary = "清除用户缓存", description = "清除指定用户的缓存")
    public Result<Void> clearUserCache(@PathVariable(name = "userId") Long userId) {
        userService.clearUserCache(userId);
        return Result.success();
    }
}
