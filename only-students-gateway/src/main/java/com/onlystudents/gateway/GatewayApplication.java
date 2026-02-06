package com.onlystudents.gateway;

import com.alibaba.cloud.sentinel.gateway.scg.SentinelSCGAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {
        SentinelSCGAutoConfiguration.class  // 禁用 Sentinel SCG 自动配置
})
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
