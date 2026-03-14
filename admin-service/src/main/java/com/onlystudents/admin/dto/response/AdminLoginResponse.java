package com.onlystudents.admin.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminLoginResponse {

    private String token;

    private Long expireTime;

    private AdminUserInfo userInfo;

    @Data
    public static class AdminUserInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String avatar;
        private Long roleId;
        private Integer status;
        private String roleName;
        private List<String> permissions;
        private LocalDateTime lastLoginTime;
    }
}
