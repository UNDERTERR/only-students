package com.onlystudents.notification.config;

import com.onlystudents.common.utils.JsonSerializerUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String FAVORITE_QUEUE = "rating.favorite.queue";
    public static final String FAVORITE_EXCHANGE = "rating.exchange";
    public static final String FAVORITE_ROUTING_KEY = "favorite.created";
    
    public static final String NOTE_PUBLISH_QUEUE = "note.publish.queue";
    public static final String NOTE_EXCHANGE = "note.exchange";
    public static final String NOTE_PUBLISH_ROUTING_KEY = "note.publish";
    
    @Bean
    public Queue favoriteQueue() {
        return QueueBuilder.durable(FAVORITE_QUEUE).build();
    }
    
    @Bean
    public TopicExchange ratingExchange() {
        return new TopicExchange(FAVORITE_EXCHANGE, true, false);
    }
    
    @Bean
    public Binding favoriteBinding(Queue favoriteQueue, TopicExchange ratingExchange) {
        return BindingBuilder.bind(favoriteQueue).to(ratingExchange).with(FAVORITE_ROUTING_KEY);
    }
    
    @Bean
    public Queue notePublishQueue() {
        return QueueBuilder.durable(NOTE_PUBLISH_QUEUE).build();
    }
    
    @Bean
    public DirectExchange noteExchange() {
        return new DirectExchange(NOTE_EXCHANGE, true, false);
    }
    
    @Bean
    public Binding notePublishBinding(Queue notePublishQueue, DirectExchange noteExchange) {
        return BindingBuilder.bind(notePublishQueue).to(noteExchange).with(NOTE_PUBLISH_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(JsonSerializerUtils.getGlobalObjectMapper());
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
    
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
