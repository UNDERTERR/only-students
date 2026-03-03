package com.onlystudents.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "登录请求")
@Data
public class LoginRequest {
    
    @Schema(description = "账号（邮箱或手机号）", example = "13800138000 或 test@example.com")
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$|^1[3-9]\\d{9}$", 
             message = "请输入正确的邮箱或手机号")
    private String account;
    
    @Schema(description = "登录方式：PASSWORD（密码登录）/SMS_CODE（验证码登录）", example = "PASSWORD")
    @NotBlank(message = "登录方式不能为空")
    private String loginType;
    
    @Schema(description = "密码登录时必填", example = "123456")
    private String password;
    
    @Schema(description = "验证码登录时必填", example = "123456")
    private String smsCode;
    
    private String deviceId;
    
    private Integer deviceType;
    
    private String deviceName;
    
    private String ip;
}
