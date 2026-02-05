package com.onlystudents.admin.service;

import com.onlystudents.admin.dto.request.AdminLoginRequest;
import com.onlystudents.admin.dto.response.AdminLoginResponse;
import com.onlystudents.admin.entity.AdminUser;

import java.util.List;

public interface AdminUserService {

    AdminLoginResponse login(AdminLoginRequest request, String ip);

    void logout(Long adminId);

    AdminUser getAdminById(Long adminId);

    List<AdminUser> getAdminList();

    void createAdmin(AdminUser adminUser);

    void updateAdmin(AdminUser adminUser);

    void deleteAdmin(Long adminId);

    void updateStatus(Long adminId, Integer status);
}
