package com.onlystudents.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlystudents.common.utils.JsonSerializerUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson配置类 - 使用Common模块的ObjectMapper
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // 使用Common模块中已配置好的ObjectMapper（支持LocalDateTime）
        return JsonSerializerUtils.getGlobalObjectMapper();
    }
}
