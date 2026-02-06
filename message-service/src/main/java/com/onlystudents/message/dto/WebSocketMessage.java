package com.onlystudents.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * WebSocket消息传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    /**
     * 消息类型
     * CONNECTED - 连接成功
     * CHAT - 聊天消息
     * MESSAGE - 新消息通知
     * HEARTBEAT - 心跳
     * HEARTBEAT_ACK - 心跳响应
     * READ - 已读回执
     * ERROR - 错误
     */
    private String type;
    
    /**
     * 消息数据
     */
    private Map<String, Object> data;
}
