package com.onlystudents.rating.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.onlystudents.rating.mapper")
public class MyBatisConfig {
}
