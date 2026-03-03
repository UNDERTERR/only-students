package com.onlystudents.withdrawal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.onlystudents.withdrawal", "com.onlystudents.common"})
@EnableDiscoveryClient
@MapperScan("com.onlystudents.withdrawal.mapper")
public class WithdrawalServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WithdrawalServiceApplication.class, args);
    }
}
