package com.onlystudents.message.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlystudents.message.dto.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 管理用户连接和消息推送，不涉及业务逻辑
 */
@Slf4j
@Component
public class WebSocketSessionManager {
    
    private final ObjectMapper objectMapper;
    
    // 存储所有在线用户的会话（用户ID -> WebSocketSession）
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();
    
    // 存储会话ID到用户ID的映射
    private static final Map<String, Long> SESSION_USER_MAP = new ConcurrentHashMap<>();
    
    public WebSocketSessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 注册用户连接
     */
    public void registerSession(Long userId, WebSocketSession session) {
        // 如果用户已有连接，先关闭旧连接
        WebSocketSession oldSession = USER_SESSIONS.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close();
            } catch (IOException e) {
                log.warn("关闭旧WebSocket连接失败", e);
            }
        }
        
        USER_SESSIONS.put(userId, session);
        SESSION_USER_MAP.put(session.getId(), userId);
        log.info("用户 {} WebSocket连接已注册，当前在线: {}", userId, USER_SESSIONS.size());
    }
    
    /**
     * 移除用户连接
     */
    public void removeSession(String sessionId) {
        Long userId = SESSION_USER_MAP.remove(sessionId);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("用户 {} WebSocket连接已移除，当前在线: {}", userId, USER_SESSIONS.size());
        }
    }
    
    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(Long userId, WebSocketMessage message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String payload = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(payload));
                log.debug("WebSocket消息已发送给用户 {}: {}", userId, message.getType());
            } catch (IOException e) {
                log.error("发送WebSocket消息失败，用户: {}", userId, e);
            }
        } else {
            log.debug("用户 {} 不在线，消息无法推送", userId);
        }
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return USER_SESSIONS.size();
    }
    
    /**
     * 获取用户ID（通过sessionId）
     */
    public Long getUserIdBySessionId(String sessionId) {
        return SESSION_USER_MAP.get(sessionId);
    }
    
    /**
     * 获取所有在线用户ID
     */
    public Map<Long, WebSocketSession> getOnlineUsers() {
        return new ConcurrentHashMap<>(USER_SESSIONS);
    }
}
