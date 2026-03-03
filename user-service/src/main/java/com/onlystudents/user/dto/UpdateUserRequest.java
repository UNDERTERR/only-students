package com.onlystudents.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新用户信息请求")
public class UpdateUserRequest {
    
    @Size(max = 50, message = "昵称长度不能超过50")
    @Schema(description = "昵称", required = false)
    private String nickname;
    
    @Schema(description = "头像URL", required = false)
    private String avatar;
    
    @Size(max = 500, message = "简介长度不能超过500")
    @Schema(description = "个人简介", required = false)
    private String bio;
    
    @Schema(description = "邮箱", required = false)
    private String email;
    
    @Schema(description = "手机号", required = false)
    private String phone;
    
    @Schema(description = "学段: 1-小学 2-初中 3-高中 4-本科 5-硕士 6-博士", required = false)
    private Integer educationLevel;
    
    @Schema(description = "学校ID", required = false)
    private Long schoolId;
    
    @Schema(description = "学校名称", required = false)
    private String schoolName;
    
    @Schema(description = "绑定手机/邮箱时的验证码", required = false)
    private String verifyCode;
}
