package com.onlystudents.user.service;

import com.onlystudents.user.dto.*;

import java.util.List;

public interface UserService {
    
    UserResponse register(RegisterRequest request);
    
    LoginResponse login(LoginRequest request);
    
    UserResponse getUserById(Long userId);
    
    UserResponse getCurrentUser(Long userId);
    
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    
    void logout(Long userId, String deviceId);
    
    void logoutAllDevices(Long userId);
    
    /**
     * 搜索用户
     */
    List<UserResponse> searchUsers(String keyword, Integer educationLevel, Integer isCreator, Integer page, Integer size);
    
    /**
     * 批量获取用户
     */
    List<UserResponse> getUsersByIds(List<Long> userIds);
    
    /**
     * 增加粉丝数
     */
    void incrementFollowerCount(Long userId);
    
    /**
     * 减少粉丝数
     */
    void decrementFollowerCount(Long userId);
    
    /**
     * 清除用户缓存
     */
    void clearUserCache(Long userId);
    
    /**
     * 验证码重置密码
     */
    void resetPasswordByCode(String account, String verifyCode, String newPassword);
    
    /**
     * 修改密码（需要旧密码）
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
