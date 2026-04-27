package com.example.payment.paymet.dto;

public record AuthValidateTokenResponse(boolean valid, String username, String message) {
}
