package com.onlystudents.rating.config;

import com.onlystudents.common.utils.JsonSerializerUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 * 配置评分相关的事件队列
 */
@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(JsonSerializerUtils.getGlobalObjectMapper());
    }
    /**
     * 评分服务交换机
     */
    @Bean
    public TopicExchange ratingExchange() {
        return new TopicExchange("rating.exchange", true, false);
    }
    
    // ==================== 收藏事件队列 ====================
    
    @Bean
    public Queue favoriteCreatedQueue() {
        return new Queue("favorite.created.queue", true);
    }
    
    @Bean
    public Queue favoriteDeletedQueue() {
        return new Queue("favorite.deleted.queue", true);
    }
    
    @Bean
    public Queue ratingFavoriteQueue() {
        return new Queue("rating.favorite.queue", true);
    }
    
    @Bean
    public Binding favoriteCreatedBinding() {
        return BindingBuilder.bind(favoriteCreatedQueue())
                .to(ratingExchange())
                .with("favorite.created");
    }
    
    @Bean
    public Binding favoriteDeletedBinding() {
        return BindingBuilder.bind(favoriteDeletedQueue())
                .to(ratingExchange())
                .with("favorite.deleted");
    }
    
    @Bean
    public Binding ratingFavoriteBinding() {
        return BindingBuilder.bind(ratingFavoriteQueue())
                .to(ratingExchange())
                .with("favorite.created");
    }
    
    // ==================== 评分事件队列 ====================
    
    @Bean
    public Queue ratingUpdatedQueue() {
        return new Queue("rating.updated.queue", true);
    }
    
    @Bean
    public Binding ratingUpdatedBinding() {
        return BindingBuilder.bind(ratingUpdatedQueue())
                .to(ratingExchange())
                .with("rating.updated");
    }
    
    // ==================== 分享事件队列 ====================
    
    @Bean
    public Queue shareCreatedQueue() {
        return new Queue("share.created.queue", true);
    }
    
    @Bean
    public Binding shareCreatedBinding() {
        return BindingBuilder.bind(shareCreatedQueue())
                .to(ratingExchange())
                .with("share.created");
    }
}
