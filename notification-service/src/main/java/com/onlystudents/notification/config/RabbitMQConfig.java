package com.onlystudents.notification.config;

import com.onlystudents.common.utils.JsonSerializerUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // ========== 收藏通知队列 ==========
    @Bean
    public Queue favoriteNotifyQueue() {
        return QueueBuilder.durable("favorite.notify.queue").build();
    }
    
    // ========== 评论通知队列 ==========
    @Bean
    public Queue commentNotifyQueue() {
        return QueueBuilder.durable("comment.notify.queue").build();
    }
    
    // ========== 粉丝通知队列 ==========
    @Bean
    public Queue followerNotifyQueue() {
        return QueueBuilder.durable("follower.notify.queue").build();
    }
    
    // ========== 私信通知队列 ==========
    @Bean
    public Queue messageNotifyQueue() {
        return QueueBuilder.durable("message.notify.queue").build();
    }
    
    // ========== 交换机 ==========
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification.exchange", true, false);
    }
    
    // ========== 绑定 ==========
    @Bean
    public Binding favoriteNotifyBinding(@Qualifier("favoriteNotifyQueue") Queue queue, @Qualifier("notificationExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("favorite.notify");
    }
    
    @Bean
    public Binding commentNotifyBinding(@Qualifier("commentNotifyQueue") Queue queue, @Qualifier("notificationExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("comment.notify");
    }
    
    @Bean
    public Binding followerNotifyBinding(@Qualifier("followerNotifyQueue") Queue queue, @Qualifier("notificationExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("follower.notify");
    }
    
    @Bean
    public Binding messageNotifyBinding(@Qualifier("messageNotifyQueue") Queue queue, @Qualifier("notificationExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("message.notify");
    }
    
    // ========== 旧配置（兼容） ==========
    @Bean
    public Queue favoriteQueue() {
        return QueueBuilder.durable("favorite.created.queue").build();
    }

    @Bean
    public TopicExchange ratingExchange() {
        return new TopicExchange("rating.exchange", true, false);
    }

    @Bean
    public Binding favoriteBinding(@Qualifier("favoriteQueue") Queue favoriteQueue, @Qualifier("ratingExchange") TopicExchange ratingExchange) {
        return BindingBuilder.bind(favoriteQueue).to(ratingExchange).with("favorite.created");
    }

    @Bean
    public Queue notePublishQueue() {
        return QueueBuilder.durable( "note.publish.queue").build();
    }

    @Bean
    public TopicExchange noteExchange() {
        return new TopicExchange("note.exchange", true, false);
    }

    @Bean
    public Binding notePublishBinding(@Qualifier("notePublishQueue") Queue notePublishQueue, @Qualifier("noteExchange") TopicExchange noteExchange) {
        return BindingBuilder.bind(notePublishQueue).to(noteExchange).with("note.publish");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(JsonSerializerUtils.getGlobalObjectMapper());
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
