package com.onlystudents.admin.service.impl;

import com.onlystudents.admin.entity.AdminRole;
import com.onlystudents.admin.mapper.AdminRoleMapper;
import com.onlystudents.admin.service.AdminRoleService;
import com.onlystudents.common.core.exception.BusinessException;
import com.onlystudents.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRoleServiceImpl implements AdminRoleService {

    private final AdminRoleMapper adminRoleMapper;

    @Override
    public AdminRole getRoleById(Long roleId) {
        return adminRoleMapper.selectById(roleId);
    }

    @Override
    public List<AdminRole> getAllRoles() {
        return adminRoleMapper.selectAllActiveRoles();
    }

    @Override
    @Transactional
    public void createRole(AdminRole role) {
        // 检查角色编码是否已存在
        if (adminRoleMapper.selectByRoleCode(role.getRoleCode()) != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色编码已存在");
        }

        role.setStatus(1);
        adminRoleMapper.insert(role);
    }

    @Override
    @Transactional
    public void updateRole(AdminRole role) {
        AdminRole existing = adminRoleMapper.selectById(role.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在");
        }

        // 如果修改了角色编码，检查是否与其他角色冲突
        if (!existing.getRoleCode().equals(role.getRoleCode())) {
            AdminRole conflict = adminRoleMapper.selectByRoleCode(role.getRoleCode());
            if (conflict != null && !conflict.getId().equals(role.getId())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "角色编码已存在");
            }
        }

        adminRoleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        adminRoleMapper.deleteById(roleId);
    }

    @Override
    public List<String> getRolePermissions(Long roleId) {
        AdminRole role = adminRoleMapper.selectById(roleId);
        if (role == null || role.getPermissions() == null) {
            return List.of();
        }
        return Arrays.asList(role.getPermissions().split(","));
    }
}
