package com.example.payment.paymet.dto;

import com.example.payment.paymet.entity.PaymentStatus;

public record UpdatePaymentStatusRequest(PaymentStatus status) {
}
