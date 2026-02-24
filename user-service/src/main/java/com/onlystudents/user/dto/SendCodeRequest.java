package com.onlystudents.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "发送验证码请求")
@Data
public class SendCodeRequest {

    @Schema(description = "账号（邮箱或手机号）", example = "13800138000 或 test@example.com")
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$|^1[3-9]\\d{9}$", 
             message = "请输入正确的邮箱或手机号")
    private String account;

    @Schema(description = "验证码类型：REGISTER/LOGIN/RESET_PWD", example = "REGISTER")
    @NotBlank(message = "验证码类型不能为空")
    private String type;
}
