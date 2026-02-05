package com.onlystudents.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {


    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    private String nickname;
    
    private String avatar;
    
    private String bio;
    
    private Integer educationLevel;
    
    private Long schoolId;
    
    private String schoolName;
    
    private Integer isCreator;
    
    private String creatorConfig;
    
    private Integer status;
    
    private LocalDateTime lastLoginTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
