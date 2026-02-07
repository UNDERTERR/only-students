package com.onlystudents.user.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.user.dto.request.LoginRequest;
import com.onlystudents.user.dto.request.RegisterRequest;
import com.onlystudents.user.dto.response.LoginResponse;
import com.onlystudents.user.dto.response.UserResponse;
import com.onlystudents.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、登出等接口")
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
    
    @GetMapping("/{userId}")
    @Operation(summary = "获取指定用户信息", description = "根据用户ID获取用户公开信息")
    public Result<UserResponse> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
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
}
