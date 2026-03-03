package com.onlystudents.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.onlystudents.file", "com.onlystudents.common"})
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.onlystudents.file.mapper")
public class FileServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}
