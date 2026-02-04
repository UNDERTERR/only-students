package com.onlystudents.admin.service.impl;

import com.onlystudents.admin.dto.request.AdminLoginRequest;
import com.onlystudents.admin.dto.response.AdminLoginResponse;
import com.onlystudents.admin.entity.AdminRole;
import com.onlystudents.admin.entity.AdminUser;
import com.onlystudents.admin.mapper.AdminRoleMapper;
import com.onlystudents.admin.mapper.AdminUserMapper;
import com.onlystudents.admin.service.AdminUserService;
import com.onlystudents.common.core.exception.BusinessException;
import com.onlystudents.common.core.result.ResultCode;
import com.onlystudents.common.web.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Import(JwtUtils.class)
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String ADMIN_TOKEN_PREFIX = "admin:token:";
    private static final long ADMIN_TOKEN_EXPIRE = 2 * 60 * 60; // 2小时

    @Override
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request, String ip) {
        // 验证验证码
        String captchaKey = request.getCaptchaKey();
        String captcha = request.getCaptcha();
        if (captchaKey != null && captcha != null) {
            String storedCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + captchaKey);
            if (storedCaptcha == null || !storedCaptcha.equalsIgnoreCase(captcha)) {
                throw new BusinessException(ResultCode.CAPTCHA_ERROR);
            }
            redisTemplate.delete("captcha:" + captchaKey);
        }

        // 查找管理员
        AdminUser adminUser = adminUserMapper.selectByUsername(request.getUsername());
        if (adminUser == null) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // 检查状态
        if (adminUser.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // 更新登录信息
        adminUserMapper.updateLoginInfo(adminUser.getId(), ip);
        
        // 生成Token
        String token = jwtUtils.generateToken(adminUser.getId(), adminUser.getUsername());

        // 存储到Redis
        redisTemplate.opsForValue().set(ADMIN_TOKEN_PREFIX + adminUser.getId(), token, ADMIN_TOKEN_EXPIRE, TimeUnit.SECONDS);

        // 获取角色信息
        AdminRole role = adminRoleMapper.selectById(adminUser.getRoleId());
        List<String> permissions = role != null && role.getPermissions() != null
                ? Arrays.asList(role.getPermissions().split(","))
                : List.of();

        // 构建响应
        AdminLoginResponse response = new AdminLoginResponse();
        response.setToken(token);
        response.setExpireTime(System.currentTimeMillis() + ADMIN_TOKEN_EXPIRE * 1000);

        AdminLoginResponse.AdminUserInfo userInfo = new AdminLoginResponse.AdminUserInfo();
        BeanUtils.copyProperties(adminUser, userInfo);
        userInfo.setRoleName(role != null ? role.getRoleName() : "");
        userInfo.setPermissions(permissions);
        response.setUserInfo(userInfo);

        return response;
    }

    @Override
    public void logout(Long adminId) {
        redisTemplate.delete(ADMIN_TOKEN_PREFIX + adminId);
    }

    @Override
    public AdminUser getAdminById(Long adminId) {
        return adminUserMapper.selectById(adminId);
    }

    @Override
    public List<AdminUser> getAdminList() {
        return adminUserMapper.selectList(null);
    }

    @Override
    @Transactional
    public void createAdmin(AdminUser adminUser) {
        // 检查用户名是否已存在
        if (adminUserMapper.selectByUsername(adminUser.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        // 加密密码
        adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        adminUser.setStatus(1);

        adminUserMapper.insert(adminUser);
    }

    @Override
    @Transactional
    public void updateAdmin(AdminUser adminUser) {
        AdminUser existing = adminUserMapper.selectById(adminUser.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 如果密码不为空，加密新密码
        if (adminUser.getPassword() != null && !adminUser.getPassword().isEmpty()) {
            adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        } else {
            adminUser.setPassword(null);
        }

        adminUserMapper.updateById(adminUser);
    }

    @Override
    @Transactional
    public void deleteAdmin(Long adminId) {
        adminUserMapper.deleteById(adminId);
    }

    @Override
    @Transactional
    public void updateStatus(Long adminId, Integer status) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(adminId);
        adminUser.setStatus(status);
        adminUserMapper.updateById(adminUser);
    }
}
