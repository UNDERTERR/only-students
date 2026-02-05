package com.onlystudents.file.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class
RabbitConfig {
    
    @Bean
    public Queue fileConvertQueue() {
        return new Queue("file.convert.queue", true);
    }
    
    @Bean
    public DirectExchange fileExchange() {
        return new DirectExchange("file.exchange", true, false);
    }
    
    @Bean
    public Binding fileConvertBinding() {
        return BindingBuilder.bind(fileConvertQueue())
                .to(fileExchange())
                .with("file.convert");
    }
}
