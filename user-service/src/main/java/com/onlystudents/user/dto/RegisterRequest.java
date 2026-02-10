package com.onlystudents.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32之间")
    private String password;
    
    private String email;
    
    private String phone;
    
    private String nickname;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
}
