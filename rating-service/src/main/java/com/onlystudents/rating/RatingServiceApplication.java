package com.onlystudents.rating;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.onlystudents.rating", "com.onlystudents.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.onlystudents.rating.mapper")
public class RatingServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RatingServiceApplication.class, args);
    }
}
