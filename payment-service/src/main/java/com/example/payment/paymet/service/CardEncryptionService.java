package com.example.payment.paymet.service;

import org.springframework.stereotype.Service;

@Service
public class CardEncryptionService {

    private final EncryptionServiceClient encryptionServiceClient;

    public CardEncryptionService(EncryptionServiceClient encryptionServiceClient) {
        this.encryptionServiceClient = encryptionServiceClient;
    }

    /**
     * Encrypts via the remote encryption-service HTTP API before the caller persists card data.
     */
    public String encrypt(String plainText) {
        return encryptionServiceClient.encrypt(plainText);
    }
}
