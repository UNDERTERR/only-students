package com.onlystudents.rating;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.onlystudents.rating.mapper")
public class RatingServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RatingServiceApplication.class, args);
    }
}
