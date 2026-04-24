package com.example.processor.dto;

import java.util.UUID;

/**
 * JSON shape must match payment-service event: {@code {"paymentId":"<uuid>"}}.
 */
public record PaymentCreatedEvent(UUID paymentId) {
}
