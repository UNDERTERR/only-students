package com.onlystudents.common.web.config;

import feign.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置类
 * 启用 Feign 客户端并配置日志级别
 */
@Slf4j
@Configuration
@EnableFeignClients
public class FeignConfig {

    /**
     * 配置 Feign 日志级别
     * NONE: 不记录任何日志（性能最高，适用于生产环境）
     * BASIC: 仅记录请求方法、URL、响应状态码和执行时间
     * HEADERS: 记录基本信息以及请求和响应头信息
     * FULL: 记录请求和响应的完整信息，包括头信息、正文和元数据
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
