package com.onlystudents.analytics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.onlystudents.analytics", "com.onlystudents.common"})
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackages = "com.onlystudents.analytics.client")
@MapperScan("com.onlystudents.analytics.mapper")
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
