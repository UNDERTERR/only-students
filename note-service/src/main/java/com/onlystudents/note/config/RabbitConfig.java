package com.onlystudents.note.config;


import com.onlystudents.common.utils.JsonSerializerUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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
     * 笔记发布成功队列
     */
    @Bean
    public Queue notePublishQueue() {
        return new Queue("note.publish.queue", true);
    }

    /**
     * 笔记交换机（使用Topic类型，支持通配符）
     */
    @Bean
    public TopicExchange noteExchange() {
        return new TopicExchange("note.exchange", true, false);
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

    /**
     * 绑定发布队列到交换机
     */
    @Bean
    public Binding notePublishBinding() {
        return BindingBuilder.bind(notePublishQueue())
                .to(noteExchange())
                .with("note.publish");
    }

    // ==================== 接收评分服务的事件队列 ====================

    /**
     * 评分服务交换机（用于接收rating-service的事件）
     */
    @Bean
    public TopicExchange ratingExchange() {
        return new TopicExchange("rating.exchange", true, false);
    }

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
     * 绑定收藏创建队列到评分交换机
     */
    @Bean
    public Binding favoriteCreatedBinding() {
        return BindingBuilder.bind(favoriteCreatedQueue())
                .to(ratingExchange())
                .with("favorite.created");
    }

    /**
     * 绑定收藏删除队列到评分交换机
     */
    @Bean
    public Binding favoriteDeletedBinding() {
        return BindingBuilder.bind(favoriteDeletedQueue())
                .to(ratingExchange())
                .with("favorite.deleted");
    }

    /**
     * 评分更新队列
     */
    @Bean
    public Queue ratingUpdatedQueue() {
        return new Queue("rating.updated.queue", true);
    }

    /**
     * 绑定评分更新队列到评分交换机
     */
    @Bean
    public Binding ratingUpdatedBinding() {
        return BindingBuilder.bind(ratingUpdatedQueue())
                .to(ratingExchange())
                .with("rating.updated");
    }

    /**
     * 分享创建队列
     */
    @Bean
    public Queue shareCreatedQueue() {
        return new Queue("share.created.queue", true);
    }

    /**
     * 绑定分享创建队列到评分交换机
     */
    @Bean
    public Binding shareCreatedBinding() {
        return BindingBuilder.bind(shareCreatedQueue())
                .to(ratingExchange())
                .with("share.created");
    }

    /**
     * JSON消息转换器
     * 用于将对象序列化为JSON格式发送，支持Java 8日期时间类型
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(JsonSerializerUtils.getGlobalObjectMapper());
    }


    /**
     * 配置 RabbitListenerContainerFactory 使用纯文本消息转换器
     * 用于 @RabbitListener 注解的方法接收消息时不自动反序列化
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter() );
        return factory;
    }
}