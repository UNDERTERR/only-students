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
    List<UserResponse> searchUsers(String keyword, Integer educationLevel, Integer page, Integer size);
    
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

    /**
     * 增加学校笔记数
     */
    void incrementSchoolNotes(Long schoolId);

    /**
     * 减少学校笔记数
     */
    void decrementSchoolNotes(Long schoolId);
    
    /**
     * 获取用户统计数据
     */
    UserStatsDTO getUserStats();
    
    /**
     * 分页查询用户列表（管理员）
     */
    List<UserResponse> getUserListPage(Integer page, Integer size, String keyword, Integer status);
    
    /**
     * 统计用户数量（管理员）
     */
    Long countUsers(Integer status);
    
    /**
     * 封禁/解封用户
     */
    void updateUserStatus(Long userId, Integer status);
    
    /**
     * 设置用户手机号
     */
    void updateUserPhone(Long userId, String phone);
    
    /**
     * 设置用户邮箱
     */
    void updateUserEmail(Long userId, String email);
    
    /**
     * 设置封禁时间（普通举报封禁）
     * @param userId 用户ID
     * @param banTime 封禁截止时间
     * @param reason 封禁原因
     */
    void setUserBan(Long userId, java.time.LocalDateTime banTime, String reason);
    
    /**
     * 直接冻结用户
     * @param userId 用户ID
     * @param freezeTime 冻结截止时间
     * @param reason 冻结原因
     */
    void setUserFreeze(Long userId, java.time.LocalDateTime freezeTime, String reason);
    
    /**
     * 解冻/解禁用户
     */
    void unfreezeUser(Long userId);
    
    /**
     * 检查用户是否可以发布内容
     * @param userId 用户ID
     * @return 如果可以发布返回true，被封禁/冻结返回false
     */
    Boolean canUserPost(Long userId);
}
