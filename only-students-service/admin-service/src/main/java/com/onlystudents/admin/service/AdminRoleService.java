package com.onlystudents.admin.service;

import com.onlystudents.admin.entity.AdminRole;

import java.util.List;

public interface AdminRoleService {

    AdminRole getRoleById(Long roleId);

    List<AdminRole> getAllRoles();

    void createRole(AdminRole role);

    void updateRole(AdminRole role);

    void deleteRole(Long roleId);

    List<String> getRolePermissions(Long roleId);
}
