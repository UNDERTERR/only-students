package com.onlystudents.note.config;


import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue noteSyncQueue() {
        return new Queue("note.sync.queue");
    }
}
