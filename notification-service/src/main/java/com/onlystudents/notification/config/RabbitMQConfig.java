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
    public DirectExchange noteExchange() {
        return new DirectExchange("note.exchange", true, false);
    }

    @Bean
    public Binding notePublishBinding(@Qualifier("notePublishQueue") Queue notePublishQueue, @Qualifier("noteExchange") DirectExchange noteExchange) {
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
