package com.onlystudents.user.service;

import com.onlystudents.user.dto.request.LoginRequest;
import com.onlystudents.user.dto.request.RegisterRequest;
import com.onlystudents.user.dto.request.UpdateUserRequest;
import com.onlystudents.user.dto.response.LoginResponse;
import com.onlystudents.user.dto.response.UserResponse;

public interface UserService {
    
    UserResponse register(RegisterRequest request);
    
    LoginResponse login(LoginRequest request);
    
    UserResponse getUserById(Long userId);
    
    UserResponse getCurrentUser(Long userId);
    
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    
    void logout(Long userId, String deviceId);
    
    void logoutAllDevices(Long userId);
}
