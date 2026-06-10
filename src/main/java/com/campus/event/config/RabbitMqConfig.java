package com.campus.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EVENT_EXCHANGE = "campus.event.exchange";

    public static final String COMMENT_CREATED_QUEUE = "campus.comment.created.queue";
    public static final String LIKE_CREATED_QUEUE = "campus.like.created.queue";
    public static final String POST_CREATED_QUEUE = "campus.post.created.queue";

    public static final String COMMENT_CREATED_ROUTING_KEY = "comment.created";
    public static final String LIKE_CREATED_ROUTING_KEY = "like.created";
    public static final String POST_CREATED_ROUTING_KEY = "post.created";

    @Bean
    public TopicExchange campusEventExchange() {
        return new TopicExchange(EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue commentCreatedQueue() {
        return new Queue(COMMENT_CREATED_QUEUE, true);
    }

    @Bean
    public Queue likeCreatedQueue() {
        return new Queue(LIKE_CREATED_QUEUE, true);
    }

    @Bean
    public Queue postCreatedQueue() {
        return new Queue(POST_CREATED_QUEUE, true);
    }

    @Bean
    public Binding commentCreatedBinding(Queue commentCreatedQueue, TopicExchange campusEventExchange) {
        return BindingBuilder.bind(commentCreatedQueue)
                .to(campusEventExchange)
                .with(COMMENT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding likeCreatedBinding(Queue likeCreatedQueue, TopicExchange campusEventExchange) {
        return BindingBuilder.bind(likeCreatedQueue)
                .to(campusEventExchange)
                .with(LIKE_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding postCreatedBinding(Queue postCreatedQueue, TopicExchange campusEventExchange) {
        return BindingBuilder.bind(postCreatedQueue)
                .to(campusEventExchange)
                .with(POST_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}
