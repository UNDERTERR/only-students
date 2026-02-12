package com.onlystudents.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlystudents.common.utils.JwtUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.onlystudents.message.mapper")
@Import(JwtUtils.class)
public class MessageServiceApplication {
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(MessageServiceApplication.class, args);
    }
}
