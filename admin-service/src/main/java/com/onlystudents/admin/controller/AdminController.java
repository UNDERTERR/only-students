package com.onlystudents.admin.controller;

import com.onlystudents.admin.dto.request.AdminLoginRequest;
import com.onlystudents.admin.dto.request.AuditRequest;
import com.onlystudents.admin.dto.response.AdminLoginResponse;
import com.onlystudents.admin.entity.AdminRole;
import com.onlystudents.admin.entity.AdminUser;
import com.onlystudents.admin.entity.AuditRecord;
import com.onlystudents.admin.service.AdminRoleService;
import com.onlystudents.admin.service.AdminUserService;
import com.onlystudents.admin.service.AuditRecordService;
import com.onlystudents.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "后台管理", description = "管理员登录、角色管理、内容审核、系统配置等接口")
public class AdminController {

    private final AdminUserService adminUserService;
    private final AdminRoleService adminRoleService;
    private final AuditRecordService auditRecordService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员登录接口")
    public Result<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return Result.success(adminUserService.login(request, ip));
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员登出", description = "管理员登出接口")
    public Result<Void> logout(@RequestHeader("X-Admin-Id") Long adminId) {
        adminUserService.logout(adminId);
        return Result.success();
    }

    @GetMapping("/user/{adminId}")
    @Operation(summary = "获取管理员信息", description = "根据ID获取管理员详细信息")
    public Result<AdminUser> getAdminById(@PathVariable Long adminId) {
        return Result.success(adminUserService.getAdminById(adminId));
    }

    @GetMapping("/users")
    @Operation(summary = "获取管理员列表", description = "获取所有管理员列表")
    public Result<List<AdminUser>> getAdminList() {
        return Result.success(adminUserService.getAdminList());
    }

    @PostMapping("/user")
    @Operation(summary = "创建管理员", description = "创建新的管理员账号")
    public Result<Void> createAdmin(@RequestBody AdminUser adminUser) {
        adminUserService.createAdmin(adminUser);
        return Result.success();
    }

    @PutMapping("/user")
    @Operation(summary = "更新管理员", description = "更新管理员信息")
    public Result<Void> updateAdmin(@RequestBody AdminUser adminUser) {
        adminUserService.updateAdmin(adminUser);
        return Result.success();
    }

    @DeleteMapping("/user/{adminId}")
    @Operation(summary = "删除管理员", description = "删除指定管理员")
    public Result<Void> deleteAdmin(@PathVariable Long adminId) {
        adminUserService.deleteAdmin(adminId);
        return Result.success();
    }

    @PutMapping("/user/{adminId}/status")
    @Operation(summary = "更新管理员状态", description = "启用或禁用管理员账号")
    public Result<Void> updateAdminStatus(@PathVariable Long adminId, @RequestParam(name = "status") Integer status) {
        adminUserService.updateStatus(adminId, status);
        return Result.success();
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色信息", description = "根据ID获取角色详细信息")
    public Result<AdminRole> getRoleById(@PathVariable Long roleId) {
        return Result.success(adminRoleService.getRoleById(roleId));
    }

    @GetMapping("/roles")
    @Operation(summary = "获取角色列表", description = "获取所有有效角色列表")
    public Result<List<AdminRole>> getAllRoles() {
        return Result.success(adminRoleService.getAllRoles());
    }

    @PostMapping("/role")
    @Operation(summary = "创建角色", description = "创建新的管理角色")
    public Result<Void> createRole(@RequestBody AdminRole role) {
        adminRoleService.createRole(role);
        return Result.success();
    }

    @PutMapping("/role")
    @Operation(summary = "更新角色", description = "更新角色信息")
    public Result<Void> updateRole(@RequestBody AdminRole role) {
        adminRoleService.updateRole(role);
        return Result.success();
    }

    @DeleteMapping("/role/{roleId}")
    @Operation(summary = "删除角色", description = "删除指定角色")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        adminRoleService.deleteRole(roleId);
        return Result.success();
    }

    @GetMapping("/role/{roleId}/permissions")
    @Operation(summary = "获取角色权限", description = "获取角色的权限列表")
    public Result<List<String>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(adminRoleService.getRolePermissions(roleId));
    }

    @PostMapping("/audit")
    @Operation(summary = "创建审核记录", description = "创建内容审核记录")
    public Result<Void> createAuditRecord(@Valid @RequestBody AuditRequest request,
                                          @RequestHeader("X-Admin-Id") Long operatorId,
                                          @RequestHeader("X-Admin-Name") String operatorName) {
        auditRecordService.createAuditRecord(request, operatorId, operatorName);
        return Result.success();
    }

    @GetMapping("/audit/target")
    @Operation(summary = "获取目标审核记录", description = "获取指定目标的审核记录列表")
    public Result<List<AuditRecord>> getAuditRecordsByTarget(@RequestParam(name = "targetId") Long targetId,
                                                             @RequestParam(name = "targetType") Integer targetType) {
        return Result.success(auditRecordService.getAuditRecordsByTarget(targetId, targetType));
    }

    @GetMapping("/audit/operator/{operatorId}")
    @Operation(summary = "获取操作员审核记录", description = "获取操作员的审核记录列表")
    public Result<List<AuditRecord>> getAuditRecordsByOperator(@PathVariable Long operatorId) {
        return Result.success(auditRecordService.getAuditRecordsByOperator(operatorId));
    }

    @GetMapping("/audit/pending")
    @Operation(summary = "获取待审核记录", description = "获取待审核的内容记录列表")
    public Result<List<AuditRecord>> getPendingAuditRecords(@RequestParam(name = "status", defaultValue = "0") Integer status) {
        return Result.success(auditRecordService.getPendingAuditRecords(status));
    }

    @GetMapping("/audit/{id}")
    @Operation(summary = "获取审核记录详情", description = "获取指定审核记录的详细信息")
    public Result<AuditRecord> getAuditRecordById(@PathVariable Long id) {
        return Result.success(auditRecordService.getAuditRecordById(id));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
