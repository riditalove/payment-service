package com.example.payment.paymet.controller;

import com.example.payment.paymet.dto.CreatePaymentRequest;
import com.example.payment.paymet.dto.PaymentResponse;
import com.example.payment.paymet.dto.UpdatePaymentStatusRequest;
import com.example.payment.paymet.entity.Payment;
import com.example.payment.paymet.service.PaymentService;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.createPayment(request);
        return ResponseEntity
            .created(URI.create("/payments/" + payment.getId()))
            .body(PaymentResponse.fromEntity(payment));
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return PaymentResponse.fromEntity(paymentService.getPaymentById(id));
    }

    @PatchMapping("/{id}/status")
    public PaymentResponse updateStatus(@PathVariable UUID id, @RequestBody UpdatePaymentStatusRequest request) {
        if (request == null || request.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }
        return PaymentResponse.fromEntity(paymentService.updatePaymentStatus(id, request.status()));
    }
}
