package com.example.payment.paymet.dto;

import com.example.payment.paymet.entity.Payment;
import com.example.payment.paymet.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    BigDecimal amount,
    String currency,
    String encryptedCardNumber,
    PaymentStatus status,
    Instant createdAt
) {
    public static PaymentResponse fromEntity(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getEncryptedCardNumber(),
            payment.getStatus(),
            payment.getCreatedAt()
        );
    }
}
