package com.onlystudents.search.config;

import com.onlystudents.common.utils.JsonSerializerUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 * 定义交换机、队列和绑定关系
 */
@Configuration
public class RabbitConfig {
    
    // 用户信息变更交换机
    public static final String USER_INFO_EXCHANGE = "user.info.exchange";
    public static final String USER_INFO_UPDATE_QUEUE = "user.info.update.queue";
    public static final String USER_INFO_UPDATE_ROUTING_KEY = "user.info.updated";
    
    /**
     * 用户信息变更交换机
     */
    @Bean
    public TopicExchange userInfoExchange() {
        return new TopicExchange(USER_INFO_EXCHANGE, true, false);
    }
    
    /**
     * 用户信息更新队列
     */
    @Bean
    public Queue userInfoUpdateQueue() {
        return QueueBuilder.durable(USER_INFO_UPDATE_QUEUE).build();
    }
    
    /**
     * 绑定用户信息更新队列到交换机
     */
    @Bean
    public Binding userInfoUpdateBinding() {
        return BindingBuilder.bind(userInfoUpdateQueue())
                .to(userInfoExchange())
                .with(USER_INFO_UPDATE_ROUTING_KEY);
    }


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
}