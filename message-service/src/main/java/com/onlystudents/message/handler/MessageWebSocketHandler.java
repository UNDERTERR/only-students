package com.onlystudents.message.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlystudents.message.dto.WebSocketMessage;
import com.onlystudents.message.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * WebSocket消息处理器
 * 处理用户连接、断开、消息收发
 * 
 * 注意：使用ApplicationContext获取MessageService，避免循环依赖
 */
@Slf4j
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    
    public MessageWebSocketHandler(WebSocketSessionManager sessionManager, 
                                   ObjectMapper objectMapper,
                                   ApplicationContext applicationContext) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
    }
    
    /**
     * 延迟获取MessageService，避免循环依赖
     */
    private MessageService getMessageService() {
        return applicationContext.getBean(MessageService.class);
    }

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

        // 注册会话
        sessionManager.registerSession(userId, session);
        
        // 发送连接成功消息
        sessionManager.sendMessageToUser(userId, WebSocketMessage.builder()
                .type("CONNECTED")
                .data(Map.of("userId", userId, "onlineCount", sessionManager.getOnlineUserCount()))
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
        Long userId = sessionManager.getUserIdBySessionId(session.getId());
        sessionManager.removeSession(session.getId());
        if (userId != null) {
            log.info("用户 {} 断开WebSocket连接，关闭状态: {}", userId, status);
        }
    }

    /**
     * 传输错误时
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = sessionManager.getUserIdBySessionId(session.getId());
        log.error("WebSocket传输错误，用户: {}", userId, exception);
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Long senderId, WebSocketMessage wsMessage) {
        try {
            Long receiverId = Long.valueOf(wsMessage.getData().get("receiverId").toString());
            String content = wsMessage.getData().get("content").toString();
            
            // 保存消息到数据库（通过延迟获取的Service）
            var savedMessage = getMessageService().sendMessage(senderId, receiverId, content);
            
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
            sessionManager.sendMessageToUser(senderId, responseMessage);
            
            // 如果接收者在线，推送给接收者
            if (sessionManager.isUserOnline(receiverId)) {
                sessionManager.sendMessageToUser(receiverId, responseMessage);
            } else {
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
        sessionManager.sendMessageToUser(userId, WebSocketMessage.builder()
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
            getMessageService().markMessageAsRead(messageId, userId);
            
            log.info("用户 {} 标记消息 {} 为已读", userId, messageId);
        } catch (Exception e) {
            log.error("处理已读回执失败", e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(Long userId, String errorMsg) {
        sessionManager.sendMessageToUser(userId, WebSocketMessage.builder()
                .type("ERROR")
                .data(Map.of("message", errorMsg))
                .build());
    }

    /**
     * 从Session中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 关闭Session
     */
    private void closeSession(WebSocketSession session) {
        try {
            session.close();
        } catch (Exception e) {
            log.error("关闭WebSocketSession失败", e);
        }
    }
}
