package com.onlystudents.note.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * RabbitMQ配置类
 * 定义笔记同步队列、交换机和绑定关系
 */
@Configuration
public class RabbitConfig {
    /**
     * 笔记同步队列
     */
    @Bean
    public Queue noteSyncQueue() {
        return new Queue("note.sync.queue", true);
    }

    /**
     * 笔记交换机
     */
    @Bean
    public DirectExchange noteExchange() {
        return new DirectExchange("note.exchange", true, false);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding noteSyncBinding() {
        return BindingBuilder.bind(noteSyncQueue())
                .to(noteExchange())
                .with("note.sync");
    }
}