package com.example.auth.dto;

public record ValidateTokenResponse(boolean valid, String username, String message) {
}
