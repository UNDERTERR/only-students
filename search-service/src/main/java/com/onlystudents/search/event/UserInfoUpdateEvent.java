package com.onlystudents.search.event;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * 用户信息更新事件
 * 当用户更新昵称、头像等信息时发布到MQ
 */
@Data
public class UserInfoUpdateEvent {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 事件时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant timestamp;
}