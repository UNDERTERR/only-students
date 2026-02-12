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
}
