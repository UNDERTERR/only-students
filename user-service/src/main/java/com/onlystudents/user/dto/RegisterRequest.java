package com.onlystudents.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "注册请求")
@Data
public class RegisterRequest {
    
    @Schema(description = "账号类型：EMAIL/PHONE", example = "PHONE")
    @NotBlank(message = "账号类型不能为空")
    private String accountType;
    
    @Schema(description = "账号（邮箱或手机号）", example = "13800138000 或 test@example.com")
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$|^1[3-9]\\d{9}$", 
             message = "请输入正确的邮箱或手机号")
    private String account;
    
    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "请输入6位数字验证码")
    private String smsCode;
    
    @Schema(description = "密码（8-20位字母数字）", example = "abc123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码8-20位字母或数字")
    private String password;
    
    @Schema(description = "昵称（2-20位中英文数字）", example = "小明")
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称2-20位中英文数字组成")
    private String nickname;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
}
