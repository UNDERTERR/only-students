package com.onlystudents.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "验证码重置密码请求")
public class ResetPasswordRequest {
    
    @Schema(description = "账号（手机号或邮箱）", required = true)
    @NotBlank(message = "账号不能为空")
    private String account;
    
    @Schema(description = "验证码", required = true)
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
    
    @Schema(description = "新密码", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String newPassword;
}
