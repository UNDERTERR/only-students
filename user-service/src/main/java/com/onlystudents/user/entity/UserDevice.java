package com.onlystudents.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_device")
public class UserDevice {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String deviceId;
    
    private Integer deviceType;
    
    private String deviceName;
    
    private LocalDateTime loginTime;
    
    private LocalDateTime lastActiveTime;
    
    private String token;
    
    private String ip;
    
    private Integer status;

}
