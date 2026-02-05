package com.onlystudents.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.onlystudents.report.mapper")
public class ReportServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }
}
