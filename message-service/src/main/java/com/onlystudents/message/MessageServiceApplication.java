package com.onlystudents.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlystudents.common.utils.JwtUtils;
import com.onlystudents.common.utils.JsonSerializerUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.onlystudents.message.mapper")
@Import(JwtUtils.class)
public class MessageServiceApplication {
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // 使用Common模块中已配置好的ObjectMapper（支持LocalDateTime序列化）
        return JsonSerializerUtils.getGlobalObjectMapper();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(MessageServiceApplication.class, args);
    }
}
