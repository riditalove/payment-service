package com.example.payment.paymet.service;

import com.example.payment.paymet.dto.CreatePaymentRequest;
import com.example.payment.paymet.entity.Payment;
import com.example.payment.paymet.entity.PaymentStatus;
import com.example.payment.paymet.messaging.PaymentEventPublisher;
import com.example.payment.paymet.repository.PaymentRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CardEncryptionService cardEncryptionService;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(
        PaymentRepository paymentRepository,
        CardEncryptionService cardEncryptionService,
        PaymentEventPublisher paymentEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.cardEncryptionService = cardEncryptionService;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        validateRequest(request);

        Payment payment = new Payment();
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        payment.setEncryptedCardNumber(cardEncryptionService.encrypt(request.cardNumber().trim()));
        payment.setStatus(PaymentStatus.PENDING);
        Payment saved = paymentRepository.save(payment);
        paymentEventPublisher.publishPaymentCreated(saved.getId());
        return saved;
    }

    @Transactional
    public Payment updatePaymentStatus(UUID id, PaymentStatus status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(UUID id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private void validateRequest(CreatePaymentRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
        if (request.currency() == null || request.currency().trim().length() != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency must be a 3-letter code");
        }
        if (request.cardNumber() == null || request.cardNumber().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card number is required");
        }
    }
}
