package com.onlystudents.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.common.utils.JwtUtils;
import com.onlystudents.user.dto.request.LoginRequest;
import com.onlystudents.user.dto.request.RegisterRequest;
import com.onlystudents.user.dto.response.LoginResponse;
import com.onlystudents.user.dto.response.UserResponse;
import com.onlystudents.user.entity.User;
import com.onlystudents.user.entity.UserDevice;
import com.onlystudents.user.mapper.UserDeviceMapper;
import com.onlystudents.user.mapper.UserMapper;
import com.onlystudents.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final UserDeviceMapper deviceMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private static final int MAX_DEVICES = 3;
    
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userMapper.selectByEmail(request.getEmail()) != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "邮箱已被注册");
        }
        
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setIsCreator(0);
        
        userMapper.insert(user);
        
        return convertToResponse(user);
    }
    
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }
        
        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 处理设备登录
        if (request.getDeviceId() != null) {
            handleDeviceLogin(user.getId(), request);
        }
        
        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(convertToResponse(user));
        response.setExpireTime(System.currentTimeMillis() + 86400000);
        
        return response;
    }
    
    private void handleDeviceLogin(Long userId, LoginRequest request) {
        // 检查是否已存在该设备
        UserDevice existingDevice = deviceMapper.selectByUserIdAndDeviceId(userId, request.getDeviceId());
        
        if (existingDevice != null) {
            // 更新现有设备
            existingDevice.setLastActiveTime(LocalDateTime.now());
            existingDevice.setIp(request.getIp());
            existingDevice.setStatus(1);
            deviceMapper.updateById(existingDevice);
            return;
        }
        
        // 检查设备数量
        int deviceCount = deviceMapper.countActiveDevices(userId);
        if (deviceCount >= MAX_DEVICES) {
            // 踢掉最早的设备
            List<UserDevice> devices = deviceMapper.selectActiveDevicesByUserId(userId);
            if (!devices.isEmpty()) {
                UserDevice oldestDevice = devices.get(devices.size() - 1);
                oldestDevice.setStatus(0);
                deviceMapper.updateById(oldestDevice);
                log.info("设备数量超限，踢掉最早登录的设备: {}", oldestDevice.getDeviceId());
            }
        }
        
        // 添加新设备
        UserDevice newDevice = new UserDevice();
        newDevice.setUserId(userId);
        newDevice.setDeviceId(request.getDeviceId());
        newDevice.setDeviceType(request.getDeviceType());
        newDevice.setDeviceName(request.getDeviceName());
        newDevice.setIp(request.getIp());
        newDevice.setLoginTime(LocalDateTime.now());
        newDevice.setLastActiveTime(LocalDateTime.now());
        newDevice.setStatus(1);
        deviceMapper.insert(newDevice);
    }
    
    @Override
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public UserResponse getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToResponse(user);
    }
    
    @Override
    public UserResponse getCurrentUser(Long userId) {
        return getUserById(userId);
    }
    
    @Override
    public void logout(Long userId, String deviceId) {
        UserDevice device = deviceMapper.selectByUserIdAndDeviceId(userId, deviceId);
        if (device != null) {
            device.setStatus(0);
            deviceMapper.updateById(device);
        }
    }
    
    @Override
    public void logoutAllDevices(Long userId) {
        LambdaQueryWrapper<UserDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDevice::getUserId, userId);
        wrapper.eq(UserDevice::getStatus, 1);
        
        UserDevice device = new UserDevice();
        device.setStatus(0);
        deviceMapper.update(device, wrapper);
    }
    
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }
}
