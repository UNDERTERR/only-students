package com.onlystudents.user.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    
    private String token;
    
    private UserResponse userInfo;
    
    private Long expireTime;
}
