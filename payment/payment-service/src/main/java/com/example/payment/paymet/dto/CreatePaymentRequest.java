package com.example.payment.paymet.dto;

import java.math.BigDecimal;

public record CreatePaymentRequest(
    BigDecimal amount,
    String currency,
    String cardNumber
) {
}
