package com.onlystudents.message.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlystudents.message.dto.WebSocketMessage;
import com.onlystudents.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 * 处理用户连接、断开、消息收发
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    
    // 存储所有在线用户的会话（用户ID -> WebSocketSession）
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();
    
    // 存储会话ID到用户ID的映射
    private static final Map<String, Long> SESSION_USER_MAP = new ConcurrentHashMap<>();

    /**
     * 连接建立后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            log.error("无法获取用户ID，关闭连接");
            closeSession(session);
            return;
        }

        // 存储会话
        USER_SESSIONS.put(userId, session);
        SESSION_USER_MAP.put(session.getId(), userId);
        
        log.info("用户 {} 建立WebSocket连接，当前在线用户数: {}", userId, USER_SESSIONS.size());
        
        // 发送连接成功消息
        sendMessageToUser(userId, WebSocketMessage.builder()
                .type("CONNECTED")
                .data(Map.of("userId", userId, "onlineCount", USER_SESSIONS.size()))
                .build());
    }

    /**
     * 收到文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long senderId = getUserIdFromSession(session);
        if (senderId == null) {
            log.error("无法获取发送者ID");
            return;
        }

        try {
            // 解析消息
            WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            
            switch (wsMessage.getType()) {
                case "CHAT":
                    handleChatMessage(senderId, wsMessage);
                    break;
                case "HEARTBEAT":
                    handleHeartbeat(senderId);
                    break;
                case "READ":
                    handleReadReceipt(senderId, wsMessage);
                    break;
                default:
                    log.warn("未知的消息类型: {}", wsMessage.getType());
            }
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendErrorMessage(senderId, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 连接关闭后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = SESSION_USER_MAP.remove(session.getId());
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("用户 {} 断开WebSocket连接，当前在线用户数: {}，关闭状态: {}", 
                    userId, USER_SESSIONS.size(), status);
        }
    }

    /**
     * 传输错误时
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = SESSION_USER_MAP.get(session.getId());
        log.error("WebSocket传输错误，用户: {}", userId, exception);
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Long senderId, WebSocketMessage wsMessage) {
        try {
            Long receiverId = Long.valueOf(wsMessage.getData().get("receiverId").toString());
            String content = wsMessage.getData().get("content").toString();
            
            // 保存消息到数据库
            var savedMessage = messageService.sendMessage(senderId, receiverId, content);
            
            // 构建响应消息
            WebSocketMessage responseMessage = WebSocketMessage.builder()
                    .type("MESSAGE")
                    .data(Map.of(
                            "id", savedMessage.getId(),
                            "senderId", senderId,
                            "receiverId", receiverId,
                            "content", content,
                            "createdAt", savedMessage.getCreatedAt()
                    ))
                    .build();
            
            // 发送给发送者（确认消息已发送）
            sendMessageToUser(senderId, responseMessage);
            
            // 如果接收者在线，推送给接收者
            if (isUserOnline(receiverId)) {
                sendMessageToUser(receiverId, responseMessage);
            } else {
                // 接收者离线，可以在这里实现离线推送（APP推送、短信等）
                log.info("用户 {} 离线，消息已存储，待用户上线后拉取", receiverId);
            }
            
        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
            sendErrorMessage(senderId, "发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(Long userId) {
        sendMessageToUser(userId, WebSocketMessage.builder()
                .type("HEARTBEAT_ACK")
                .data(Map.of("timestamp", System.currentTimeMillis()))
                .build());
    }

    /**
     * 处理已读回执
     */
    private void handleReadReceipt(Long userId, WebSocketMessage wsMessage) {
        try {
            Long messageId = Long.valueOf(wsMessage.getData().get("messageId").toString());
            messageService.markMessageAsRead(messageId, userId);
            
            log.info("用户 {} 标记消息 {} 为已读", userId, messageId);
        } catch (Exception e) {
            log.error("处理已读回执失败", e);
        }
    }

    /**
     * 发送消息给指定用户（公共方法，供Service调用）
     */
    public void sendMessageToUser(Long userId, WebSocketMessage message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String payload = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(payload));
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
     * 获取在线用户列表
     */
    public Map<Long, WebSocketSession> getOnlineUsers() {
        return new ConcurrentHashMap<>(USER_SESSIONS);
    }

    /**
     * 从Session中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(Long userId, String errorMsg) {
        sendMessageToUser(userId, WebSocketMessage.builder()
                .type("ERROR")
                .data(Map.of("message", errorMsg))
                .build());
    }

    /**
     * 关闭Session
     */
    private void closeSession(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            log.error("关闭WebSocketSession失败", e);
        }
    }
}
