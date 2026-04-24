package com.example.processor.listener;

import com.example.processor.config.RabbitListenerConfiguration;
import com.example.processor.dto.PaymentCreatedEvent;
import com.example.processor.service.PaymentProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentCreatedListener.class);

    private final ObjectMapper objectMapper;
    private final PaymentProcessingService paymentProcessingService;

    public PaymentCreatedListener(ObjectMapper objectMapper, PaymentProcessingService paymentProcessingService) {
        this.objectMapper = objectMapper;
        this.paymentProcessingService = paymentProcessingService;
    }

    @RabbitListener(
        queues = RabbitListenerConfiguration.PAYMENTS_QUEUE,
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void onPaymentCreated(String body) {
        try {
            PaymentCreatedEvent event = objectMapper.readValue(body, PaymentCreatedEvent.class);
            log.info("Processing payment.created paymentId={}", event.paymentId());
            paymentProcessingService.processPaymentCreated(event);
        } catch (JsonProcessingException ex) {
            throw new AmqpRejectAndDontRequeueException("Invalid PaymentCreatedEvent JSON", ex);
        }
    }
}
