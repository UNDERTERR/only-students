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
    
    // ==================== 接收评分服务的事件队列 ====================
    
    /**
     * 收藏创建队列
     */
    @Bean
    public Queue favoriteCreatedQueue() {
        return new Queue("favorite.created.queue", true);
    }
    
    /**
     * 收藏删除队列
     */
    @Bean
    public Queue favoriteDeletedQueue() {
        return new Queue("favorite.deleted.queue", true);
    }
    
    /**
     * 评分更新队列
     */
    @Bean
    public Queue ratingUpdatedQueue() {
        return new Queue("rating.updated.queue", true);
    }
    
    /**
     * 分享创建队列
     */
    @Bean
    public Queue shareCreatedQueue() {
        return new Queue("share.created.queue", true);
    }
}