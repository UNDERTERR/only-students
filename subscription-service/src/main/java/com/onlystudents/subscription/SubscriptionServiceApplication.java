package com.onlystudents.subscription;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.onlystudents.subscription", "com.onlystudents.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.onlystudents.subscription.mapper")
public class SubscriptionServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApplication.class, args);
    }
}
