package com.example.payment.paymet.messaging;

import com.example.payment.paymet.config.RabbitMQTopologyConfiguration;
import com.example.payment.paymet.dto.PaymentCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Boot 4 + webmvc does not always expose an {@link ObjectMapper} bean; keep serialization self-contained.
     */
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes {@link PaymentCreatedEvent} as JSON (no Java type headers) for cross-service compatibility.
     */
    public void publishPaymentCreated(UUID paymentId) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(new PaymentCreatedEvent(paymentId));
            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            rabbitTemplate.send(
                RabbitMQTopologyConfiguration.PAYMENTS_EXCHANGE,
                RabbitMQTopologyConfiguration.PAYMENTS_CREATED_ROUTING_KEY,
                new Message(body, props)
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize PaymentCreatedEvent", ex);
        }
    }
}
