package com.example.processor.service;

import com.example.processor.client.PaymentServiceClient;
import com.example.processor.dto.PaymentCreatedEvent;
import com.example.processor.dto.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final PaymentServiceClient paymentServiceClient;
    private final RetryTemplate paymentRetryTemplate;

    public PaymentProcessingService(
        PaymentServiceClient paymentServiceClient,
        @Qualifier("paymentRetryTemplate") RetryTemplate paymentRetryTemplate
    ) {
        this.paymentServiceClient = paymentServiceClient;
        this.paymentRetryTemplate = paymentRetryTemplate;
    }

    public void processPaymentCreated(PaymentCreatedEvent event) {
        try {
            paymentRetryTemplate.execute(context -> {
                paymentServiceClient.updatePaymentStatus(event.paymentId(), PaymentStatus.SUCCESS);
                return null;
            });
        } catch (RuntimeException ex) {
            log.warn("Could not confirm payment {} via payment-service after retries; marking FAILED", event.paymentId(), ex);
            try {
                paymentServiceClient.updatePaymentStatus(event.paymentId(), PaymentStatus.FAILED);
            } catch (RestClientException e) {
                log.error("Failed to mark payment {} as FAILED", event.paymentId(), e);
            }
        }
    }
}
