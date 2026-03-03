package com.onlystudents.message.config;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.message.handler.MessageWebSocketHandler;
import com.onlystudents.message.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageWebSocketHandler messageWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, "/ws/message")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }

    /**
     * 配置WebSocket容器参数
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 设置消息大小限制（10MB）
        container.setMaxTextMessageBufferSize(CommonConstants.MaxTextMessageBufferSize);
        container.setMaxBinaryMessageBufferSize(CommonConstants.MaxBinaryMessageBufferSize);
        // 设置会话超时时间（30分钟）
        container.setMaxSessionIdleTimeout(CommonConstants.MaxSessionIdleTimeout);
        return container;
    }
}
