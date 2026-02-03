package com.onlystudents.report.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OnlyStudents Report Service API")
                        .version("1.0.0")
                        .description("学习笔记分享平台 - 举报服务接口文档")
                        .contact(new Contact()
                                .name("OnlyStudents Team")
                                .email("support@onlystudents.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
    
    @Bean
    public GroupedOpenApi reportApi() {
        return GroupedOpenApi.builder()
                .group("report")
                .pathsToMatch("/**")
                .build();
    }
}
