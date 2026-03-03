package com.onlystudents.message.interceptor;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket认证拦截器
 * 在握手阶段验证用户身份
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // 从URL参数或Header中获取Token
            String token = extractToken(request);
            
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket连接缺少Token");
                return false;
            }

            // 验证Token
            if (!jwtUtils.isTokenValid(token)) {
                log.warn("WebSocket连接Token无效");
                return false;
            }

            // 获取用户ID并存储到attributes中
            Long userId = jwtUtils.getUserId(token);
            attributes.put("userId", userId);
            
            log.info("WebSocket认证成功，用户ID: {}", userId);
            return true;
            
        } catch (Exception e) {
            log.error("WebSocket认证失败", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理，一般不需要
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // 1. 从URL参数中获取
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && !token.isEmpty()) {
                return token;
            }
            
            // 2. 从Header中获取
            String authHeader = servletRequest.getServletRequest().getHeader(CommonConstants.TOKEN_HEADER);
            if (authHeader != null && authHeader.startsWith(CommonConstants.TOKEN_PREFIX)) {
                return authHeader.substring(7);
            }
        }
        return null;
    }
}
