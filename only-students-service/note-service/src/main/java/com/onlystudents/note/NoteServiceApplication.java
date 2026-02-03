package com.onlystudents.note;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.onlystudents.note.mapper")
public class NoteServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NoteServiceApplication.class, args);
    }
}
