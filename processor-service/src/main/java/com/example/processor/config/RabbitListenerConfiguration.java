package com.example.processor.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitListenerConfiguration {

    public static final String PAYMENTS_QUEUE = "payments.queue";

    @Bean
    public Queue paymentsQueue() {
        return new Queue(PAYMENTS_QUEUE, true);
    }

    /**
     * String payloads + JSON parsing in the listener keeps messages compatible with payment-service
     * (plain JSON body, no Java type headers).
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }
}
