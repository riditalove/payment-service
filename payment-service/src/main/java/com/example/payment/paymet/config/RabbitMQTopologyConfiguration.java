package com.example.payment.paymet.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQTopologyConfiguration {

    public static final String PAYMENTS_EXCHANGE = "payments.exchange";
    public static final String PAYMENTS_QUEUE = "payments.queue";
    public static final String PAYMENTS_CREATED_ROUTING_KEY = "payments.created";

    @Bean
    public DirectExchange paymentsExchange() {
        return new DirectExchange(PAYMENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentsQueue() {
        return new Queue(PAYMENTS_QUEUE, true);
    }

    @Bean
    public Binding paymentsCreatedBinding(Queue paymentsQueue, DirectExchange paymentsExchange) {
        return BindingBuilder.bind(paymentsQueue).to(paymentsExchange).with(PAYMENTS_CREATED_ROUTING_KEY);
    }
}
