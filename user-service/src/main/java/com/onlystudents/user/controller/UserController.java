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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
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
                                                   @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                   @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return Result.success(userService.searchUsers(keyword, educationLevel, page, size));
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
    
    @PostMapping("/reset-password")
    @Operation(summary = "验证码重置密码", description = "通过验证码重置密码，不需要登录")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPasswordByCode(request.getAccount(), request.getVerifyCode(), request.getNewPassword());
        return Result.success();
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码，需要旧密码")
    public Result<Void> changePassword(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
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

    @PostMapping("/school/notes/increment/{schoolId}")
    @Operation(summary = "增加学校笔记数", description = "增加指定学校的笔记数量")
    public Result<Void> incrementSchoolNotes(@PathVariable(name = "schoolId") Long schoolId) {
        userService.incrementSchoolNotes(schoolId);
        return Result.success();
    }

    @PostMapping("/school/notes/decrement/{schoolId}")
    @Operation(summary = "减少学校笔记数", description = "减少指定学校的笔记数量")
    public Result<Void> decrementSchoolNotes(@PathVariable(name = "schoolId") Long schoolId) {
        userService.decrementSchoolNotes(schoolId);
        return Result.success();
    }
    
    @GetMapping("/stats")
    @Operation(summary = "获取用户统计数据", description = "获取用户统计数据，包括总用户数、新增用户数等")
    public Result<UserStatsDTO> getUserStats() {
        return Result.success(userService.getUserStats());
    }
    
    @GetMapping("/list")
    @Operation(summary = "分页获取用户列表", description = "管理员分页获取用户列表")
    public Result<java.util.Map<String, Object>> getUserList(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status) {
        List<UserResponse> list = userService.getUserListPage(page, size, keyword, status);
        Long total = userService.countUsers(status);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return Result.success(result);
    }
    
    @GetMapping("/count")
    @Operation(summary = "统计用户数量", description = "管理员统计用户数量")
    public Result<Long> countUsers(
            @RequestParam(name = "status", required = false) Integer status) {
        return Result.success(userService.countUsers(status));
    }
    
    @PutMapping("/{userId}/status")
    @Operation(summary = "更新用户状态", description = "管理员封禁/解封用户，status=0禁用，status=1启用")
    public Result<Void> updateUserStatus(@PathVariable(name = "userId") Long userId,
            @RequestParam(name = "status") Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success();
    }
    
    @PutMapping("/{userId}/phone")
    @Operation(summary = "设置用户手机号", description = "管理员设置用户手机号")
    public Result<Void> updateUserPhone(@PathVariable(name = "userId") Long userId,
            @RequestParam(name = "phone") String phone) {
        userService.updateUserPhone(userId, phone);
        return Result.success();
    }
    
    @PutMapping("/{userId}/email")
    @Operation(summary = "设置用户邮箱", description = "管理员设置用户邮箱")
    public Result<Void> updateUserEmail(@PathVariable(name = "userId") Long userId,
            @RequestParam(name = "email") String email) {
        userService.updateUserEmail(userId, email);
        return Result.success();
    }
    
    @PutMapping("/{userId}/ban")
    @Operation(summary = "设置封禁时间", description = "管理员设置用户封禁时间（普通举报封禁）")
    public Result<Void> setUserBan(@PathVariable(name = "userId") Long userId,
            @RequestParam(name = "banTime") String banTimeStr,
            @RequestParam(name = "reason", required = false) String reason) {
        java.time.LocalDateTime banTime = java.time.LocalDateTime.parse(banTimeStr.replace(" ", "T"));
        userService.setUserBan(userId, banTime, reason);
        return Result.success();
    }
    
    @PutMapping("/{userId}/freeze")
    @Operation(summary = "直接冻结用户", description = "管理员直接冻结用户")
    public Result<Void> setUserFreeze(@PathVariable(name = "userId") Long userId,
            @RequestParam(name = "freezeTime") String freezeTimeStr,
            @RequestParam(name = "reason", required = false) String reason) {
        java.time.LocalDateTime freezeTime = java.time.LocalDateTime.parse(freezeTimeStr.replace(" ", "T"));
        userService.setUserFreeze(userId, freezeTime, reason);
        return Result.success();
    }
    
    @PutMapping("/{userId}/unfreeze")
    @Operation(summary = "解冻/解禁用户", description = "管理员解冻/解禁用户")
    public Result<Void> unfreezeUser(@PathVariable(name = "userId") Long userId) {
        log.info("收到解禁请求: userId={}", userId);
        userService.unfreezeUser(userId);
        return Result.success();
    }
    
    @GetMapping("/{userId}/canPost")
    @Operation(summary = "检查用户是否可以发布内容", description = "检查用户是否被封禁/冻结")
    public Result<Boolean> canUserPost(@PathVariable(name = "userId") Long userId) {
        return Result.success(userService.canUserPost(userId));
    }
}
