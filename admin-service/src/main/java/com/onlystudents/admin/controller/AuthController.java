package com.onlystudents.admin.controller;

import com.onlystudents.admin.dto.response.AdminLoginResponse;
import com.onlystudents.admin.entity.AdminRole;
import com.onlystudents.admin.entity.AdminUser;
import com.onlystudents.admin.service.AdminRoleService;
import com.onlystudents.admin.service.AdminUserService;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "登录认证相关接口")
public class AuthController {

    private final AdminUserService adminUserService;
    private final AdminRoleService adminRoleService;
    private final JwtUtils jwtUtils;

    @GetMapping("/account/profile")
    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的基本信息")
    public Result<AdminLoginResponse.AdminUserInfo> getCurrentUserInfo(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long adminId = getAdminIdFromToken(authorization);
        
        if (adminId == null) {
            return Result.success(null);
        }
        
        AdminUser adminUser = adminUserService.getAdminById(adminId);
        if (adminUser == null) {
            return Result.success(null);
        }
        
        AdminLoginResponse.AdminUserInfo userInfo = new AdminLoginResponse.AdminUserInfo();
        userInfo.setId(adminUser.getId());
        userInfo.setUsername(adminUser.getUsername());
        userInfo.setRealName(adminUser.getRealName());
        userInfo.setAvatar(adminUser.getAvatar());
        userInfo.setPhone(adminUser.getPhone());
        userInfo.setEmail(adminUser.getEmail());
        userInfo.setRoleId(adminUser.getRoleId());
        userInfo.setStatus(adminUser.getStatus());
        
        AdminRole role = adminRoleService.getRoleById(adminUser.getRoleId());
        userInfo.setRoleName(role != null ? role.getRoleName() : "");
        
        return Result.success(userInfo);
    }

    @GetMapping("/account/permissions")
    @Operation(summary = "获取当前用户权限", description = "获取当前登录用户的权限列表")
    public Result<List<String>> getCurrentUserPermissions(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long adminId = getAdminIdFromToken(authorization);
        
        if (adminId == null) {
            return Result.success(List.of());
        }
        
        AdminUser adminUser = adminUserService.getAdminById(adminId);
        if (adminUser == null) {
            return Result.success(List.of());
        }
        
        AdminRole role = adminRoleService.getRoleById(adminUser.getRoleId());
        if (role == null || role.getPermissions() == null) {
            return Result.success(List.of());
        }
        
        List<String> permissions = Arrays.asList(role.getPermissions().split(","));
        return Result.success(permissions);
    }

    @GetMapping("/account/menus")
    @Operation(summary = "获取当前用户菜单", description = "获取当前登录用户的菜单列表")
    public Result<List<Map<String, Object>>> getCurrentUserMenus(@RequestHeader(value = "Authorization", required = false) String authorization) {
        List<Map<String, Object>> menus = new ArrayList<>();
        
        // Dashboard 菜单
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("name", "home");
        dashboard.put("path", "/home");
        dashboard.put("meta", Map.of("title", "首页", "icon", "mdi:monitor-dashboard", "order", 1));
        menus.add(dashboard);
        
        // 用户管理菜单
        Map<String, Object> userManagement = new HashMap<>();
        userManagement.put("name", "user-management");
        userManagement.put("path", "/user-management");
        userManagement.put("meta", Map.of("title", "用户管理", "icon", "mdi:account-group", "order", 2));
        menus.add(userManagement);
        
        // 内容审核菜单
        Map<String, Object> audit = new HashMap<>();
        audit.put("name", "content-audit");
        audit.put("path", "/content-audit");
        audit.put("meta", Map.of("title", "内容审核", "icon", "mdi:shield-check", "order", 3));
        menus.add(audit);
        
        // 举报管理菜单
        Map<String, Object> report = new HashMap<>();
        report.put("name", "report-management");
        report.put("path", "/report-management");
        report.put("meta", Map.of("title", "举报管理", "icon", "mdi:flag", "order", 4));
        menus.add(report);
        
        // 笔记管理菜单
        Map<String, Object> note = new HashMap<>();
        note.put("name", "note-management");
        note.put("path", "/note-management");
        note.put("meta", Map.of("title", "笔记管理", "icon", "mdi:note-text", "order", 5));
        menus.add(note);
        
        // 操作日志菜单
        Map<String, Object> log = new HashMap<>();
        log.put("name", "operation-log");
        log.put("path", "/operation-log");
        log.put("meta", Map.of("title", "操作日志", "icon", "mdi:history", "order", 6));
        menus.add(log);
        
        return Result.success(menus);
    }

    private Long getAdminIdFromToken(String authorization) {
        if (authorization != null && authorization.startsWith(CommonConstants.TOKEN_PREFIX)) {
            String token = authorization.substring(CommonConstants.TOKEN_PREFIX.length());
            return jwtUtils.getUserId(token);
        }
        return null;
    }
}
