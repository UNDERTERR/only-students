package com.onlystudents.user.config;

import com.onlystudents.common.utils.JsonSerializerUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String USER_INFO_EXCHANGE = "user.info.exchange";
    public static final String USER_INFO_UPDATE_QUEUE = "user.info.update.queue";
    public static final String USER_INFO_UPDATE_ROUTING_KEY = "user.info.updated";

    @Bean
    public TopicExchange userInfoExchange() {
        return new TopicExchange(USER_INFO_EXCHANGE, true, false);
    }

    @Bean
    public Queue userInfoUpdateQueue() {
        return QueueBuilder.durable(USER_INFO_UPDATE_QUEUE).build();
    }

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
}
