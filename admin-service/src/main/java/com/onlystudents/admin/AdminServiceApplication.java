package com.onlystudents.admin;

import com.onlystudents.common.utils.JwtUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.onlystudents.admin", "com.onlystudents.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.onlystudents.admin.client"})
@MapperScan("com.onlystudents.admin.mapper")
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
