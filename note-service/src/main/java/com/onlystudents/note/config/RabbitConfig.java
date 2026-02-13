package com.onlystudents.note.config;


import com.onlystudents.common.utils.JsonSerializerUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
     * 笔记删除队列
     */
    @Bean
    public Queue noteDeleteQueue() {
        return new Queue("note.delete.queue", true);
    }

    /**
     * 笔记交换机
     */
    @Bean
    public DirectExchange noteExchange() {
        return new DirectExchange("note.exchange", true, false);
    }

    /**
     * 绑定同步队列到交换机
     */
    @Bean
    public Binding noteSyncBinding() {
        return BindingBuilder.bind(noteSyncQueue())
                .to(noteExchange())
                .with("note.sync");
    }

    /**
     * 绑定删除队列到交换机
     */
    @Bean
    public Binding noteDeleteBinding() {
        return BindingBuilder.bind(noteDeleteQueue())
                .to(noteExchange())
                .with("note.delete");
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
    
    /**
     * JSON消息转换器
     * 用于将对象序列化为JSON格式发送，支持Java 8日期时间类型
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(JsonSerializerUtils.getGlobalObjectMapper());
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 关键1：给生产者绑定JSON转换器
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 配置 RabbitListenerContainerFactory 使用 JSON 消息转换器
     * 用于 @RabbitListener 注解的方法接收消息时自动反序列化
     */
    @Bean
    public org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory factory =
                new org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}