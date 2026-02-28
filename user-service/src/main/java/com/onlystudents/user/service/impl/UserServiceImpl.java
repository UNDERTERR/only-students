package com.onlystudents.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.common.utils.JwtUtils;
import com.onlystudents.user.dto.*;
import com.onlystudents.user.entity.User;
import com.onlystudents.user.entity.UserDevice;
import com.onlystudents.user.entity.School;
import com.onlystudents.user.event.UserEventPublisher;
import com.onlystudents.user.mapper.UserDeviceMapper;
import com.onlystudents.user.mapper.UserMapper;
import com.onlystudents.user.mapper.SchoolMapper;
import com.onlystudents.user.service.SensitiveWordFilterService;
import com.onlystudents.user.service.UserService;
import com.onlystudents.user.service.VerificationCodeService;
import com.onlystudents.user.service.VerificationCodeService.CodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserDeviceMapper deviceMapper;
    private final SchoolMapper schoolMapper;
    private final JwtUtils jwtUtils;
    private final VerificationCodeService verificationCodeService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserEventPublisher userEventPublisher;
    private final SensitiveWordFilterService sensitiveWordFilterService;
    private final CacheManager cacheManager;
    private static final int MAX_DEVICES = 3;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 验证验证码
        CodeType codeType = "EMAIL".equalsIgnoreCase(request.getAccountType()) ? CodeType.REGISTER : CodeType.REGISTER;
        if (!verificationCodeService.verifyCode(request.getAccount(), request.getSmsCode(), codeType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }

        // 根据账号类型设置邮箱或手机
        String email = null;
        String phone = null;
        if ("EMAIL".equalsIgnoreCase(request.getAccountType())) {
            email = request.getAccount();
        } else {
            phone = request.getAccount();
        }

        // 验证邮箱或手机至少有一个
        if (email == null && phone == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "邮箱或手机号不能为空");
        }

        // 检查邮箱是否已存在
        if (email != null && userMapper.selectByEmail(email) != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (phone != null && userMapper.selectByPhone(phone) != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号已被注册");
        }

        // 敏感词过滤
        String filteredNickname = sensitiveWordFilterService.filter(request.getNickname());

        // 检查昵称是否已存在
        if (userMapper.selectByNickname(filteredNickname) != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "昵称已被使用");
        }

        // 创建用户
        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setNickname(filteredNickname);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setIsCreator(0);
        user.setEducationLevel(request.getEducationLevel());

        Long schoolId = request.getSchoolId();
        String schoolName = request.getSchoolName();

        if (schoolId == null && schoolName != null && !schoolName.isEmpty()) {
            School existingSchool = schoolMapper.selectByName(schoolName);
            if (existingSchool != null) {
                schoolId = existingSchool.getId();
            }
        }

        user.setSchoolId(schoolId);
        user.setSchoolName(schoolName);

        userMapper.insert(user);

        if (schoolId != null || (schoolName != null && !schoolName.isEmpty())) {
            handleSchoolPopulationForRegister(schoolId, schoolName);
        }

        return convertToResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = null;

        // 根据登录方式验证
        if ("SMS_CODE".equalsIgnoreCase(request.getLoginType())) {
            // 验证码登录
            if (!verificationCodeService.verifyCode(request.getAccount(), request.getSmsCode(), CodeType.LOGIN)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
            }
            // 查询用户
            if (request.getAccount().contains("@")) {
                user = userMapper.selectByEmail(request.getAccount());
            } else {
                user = userMapper.selectByPhone(request.getAccount());
            }
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND, "该账号未注册");
            }
        } else {
            // 密码登录
            if (request.getAccount().contains("@")) {
                user = userMapper.selectByEmail(request.getAccount());
            } else {
                user = userMapper.selectByPhone(request.getAccount());
            }
            if (user == null) {
                throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR, "账号或密码错误");
            }
            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR, "账号或密码错误");
            }
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 处理设备登录
        if (request.getDeviceId() != null) {
            handleDeviceLogin(user.getId(), request);
        }

        // 生成Token（只存userId）
        String token = jwtUtils.generateToken(user.getId());

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
    @Cacheable(value = "users", key = "#p0", unless = "#result == null")
    public UserResponse getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        log.info("查询用户信息: userId={}, schoolId={}, schoolName={}", userId, user.getSchoolId(), user.getSchoolName());
        return convertToResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        return getUserById(userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#p0")
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新非空字段，昵称需要敏感词过滤
        if (request.getNickname() != null) {
            String filteredNickname = sensitiveWordFilterService.filter(request.getNickname());
            user.setNickname(filteredNickname);
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getEducationLevel() != null) {
            user.setEducationLevel(request.getEducationLevel());
        }

        Long newSchoolId = request.getSchoolId();
        String newSchoolName = request.getSchoolName();

        if (newSchoolId == null && newSchoolName != null && !newSchoolName.isEmpty()) {
            School existingSchool = schoolMapper.selectByName(newSchoolName);
            if (existingSchool != null) {
                newSchoolId = existingSchool.getId();
            }
        }

        if (newSchoolId != null || (newSchoolName != null && !newSchoolName.isEmpty())) {
            handleSchoolPopulation(user, newSchoolId, newSchoolName);
            user.setSchoolId(newSchoolId);
        } else if (newSchoolId == null && newSchoolName == null && user.getSchoolId() != null) {
            handleSchoolClear(user);
        }
        if (newSchoolName != null) {
            user.setSchoolName(newSchoolName);
        } else if (newSchoolName == null && user.getSchoolName() != null) {
            user.setSchoolName(null);
        }

        // 处理绑定手机号或邮箱（需要验证码）
        if (request.getVerifyCode() != null && request.getVerifyCode().length() > 0) {
            if (request.getPhone() != null && request.getPhone().length() > 0) {
                // 绑定手机号
                if (!verificationCodeService.verifyCode(request.getPhone(), request.getVerifyCode(), CodeType.BIND)) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
                }
                // 检查手机号是否已被其他用户绑定
                User existingUser = userMapper.selectByPhone(request.getPhone());
                if (existingUser != null && !existingUser.getId().equals(userId)) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "该手机号已被其他用户绑定");
                }
                user.setPhone(request.getPhone());
                log.info("用户绑定手机号: userId={}, phone={}", userId, request.getPhone());
            }

            if (request.getEmail() != null && request.getEmail().length() > 0) {
                // 绑定邮箱
                if (!verificationCodeService.verifyCode(request.getEmail(), request.getVerifyCode(), CodeType.BIND)) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
                }
                // 检查邮箱是否已被其他用户绑定
                User existingUser = userMapper.selectByEmail(request.getEmail());
                if (existingUser != null && !existingUser.getId().equals(userId)) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "该邮箱已被其他用户绑定");
                }
                user.setEmail(request.getEmail());
                log.info("用户绑定邮箱: userId={}, email={}", userId, request.getEmail());
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 发布用户信息更新事件
        userEventPublisher.publishUserInfoUpdated(user);

        return convertToResponse(user);
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

    @Override
    public List<UserResponse> searchUsers(String keyword, Integer educationLevel, Integer isCreator, Integer page, Integer size) {
        log.info("搜索用户：keyword={}, educationLevel={}, isCreator={}, page={}, size={}",
                keyword, educationLevel, isCreator, page, size);

        // 调用 Mapper 进行 MySQL 搜索
        List<User> users = userMapper.searchUsers(keyword, educationLevel, isCreator, (page - 1) * size, size);

        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void incrementFollowerCount(Long userId) {
        if (userId != null) {
            log.info("开始增加粉丝数, userId={}", userId);
            int rows = userMapper.incrementFollowerCount(userId);
            log.info("增加粉丝数结果, userId={}, rows={}", userId, rows);
            clearUserCache(userId);
            log.info("清除缓存完成, userId={}", userId);
        }
    }

    @Override
    public void decrementFollowerCount(Long userId) {
        if (userId != null) {
            userMapper.decrementFollowerCount(userId);
            clearUserCache(userId);
        }
    }

    @Override
    public void clearUserCache(Long userId) {
        log.info("清除用户缓存: userId={}", userId);
        if (cacheManager != null && userId != null) {
            Cache cache = cacheManager.getCache("users");
            if (cache != null) {
                cache.evict(userId);
                log.info("用户缓存已清除: userId={}", userId);
            }
        }
    }

    @Override
    public void resetPasswordByCode(String account, String verifyCode, String newPassword) {
        // 验证验证码
        if (!verificationCodeService.verifyCode(account, verifyCode, CodeType.RESET_PWD)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }

        // 查找用户
        User user = null;
        if (account.contains("@")) {
            user = userMapper.selectByEmail(account);
        } else {
            user = userMapper.selectByPhone(account);
        }

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "该账号不存在");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户通过验证码重置密码: userId={}, account={}", user.getId(), account);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改密码: userId={}", userId);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    private void handleSchoolPopulation(User user, Long newSchoolId, String newSchoolName) {
        Long oldSchoolId = user.getSchoolId();

        if (oldSchoolId != null && (newSchoolId == null || !oldSchoolId.equals(newSchoolId))) {
            School oldSchool = schoolMapper.selectById(oldSchoolId);
            if (oldSchool != null && oldSchool.getPopulation() > 0) {
                schoolMapper.decrementPopulation(oldSchoolId);
                log.info("用户更换/清除学校，原学校ID={}人数-1", oldSchoolId);
            }
        }

        if (newSchoolName != null && !newSchoolName.isEmpty()) {
            School school = null;

            if (newSchoolId != null) {
                school = schoolMapper.selectById(newSchoolId);
            }

            if (school == null) {
                school = schoolMapper.selectByName(newSchoolName);
            }

            if (school != null) {
                if (oldSchoolId == null || !oldSchoolId.equals(school.getId())) {
                    schoolMapper.incrementPopulation(school.getId());
                    log.info("用户选择学校ID={}人数+1", school.getId());
                }
            } else {
                School newSchool = new School();
                newSchool.setName(newSchoolName);
                newSchool.setPopulation(1);
                newSchool.setStatus(1);
                schoolMapper.insert(newSchool);
                log.info("用户新增学校ID={}人数初始化为1", newSchool.getId());
            }
        }
    }

    private void handleSchoolClear(User user) {
        Long oldSchoolId = user.getSchoolId();
        if (oldSchoolId != null) {
            School school = schoolMapper.selectById(oldSchoolId);
            if (school != null && school.getPopulation() > 0) {
                schoolMapper.decrementPopulation(oldSchoolId);
                log.info("用户清除学校，学校ID={}人数-1", oldSchoolId);
            }
        }
    }

    private void handleSchoolPopulationForRegister(Long schoolId, String schoolName) {
        if (schoolId != null) {
            School school = schoolMapper.selectById(schoolId);
            if (school != null) {
                schoolMapper.incrementPopulation(schoolId);
                log.info("用户注册选择学校ID={}人数+1", schoolId);
                return;
            }
        }

        if (schoolName != null && !schoolName.isEmpty()) {
            School school = schoolMapper.selectByName(schoolName);
            if (school != null) {
                schoolMapper.incrementPopulation(school.getId());
                log.info("用户注册选择学校名称ID={}人数+1", school.getId());
            } else {
                School newSchool = new School();
                newSchool.setName(schoolName);
                newSchool.setPopulation(1);
                newSchool.setStatus(1);
                schoolMapper.insert(newSchool);
                log.info("用户注册新增学校ID={}人数初始化为1", newSchool.getId());
            }
        }
    }

    @Override
    public void incrementSchoolNotes(Long schoolId) {
        if (schoolId != null) {
            schoolMapper.incrementNotes(schoolId);
            log.info("学校笔记数+1: schoolId={}", schoolId);
        }
    }

    @Override
    public void decrementSchoolNotes(Long schoolId) {
        if (schoolId != null) {
            schoolMapper.decrementNotes(schoolId);
            log.info("学校笔记数-1: schoolId={}", schoolId);
        }
    }
}
