package com.example.payment.paymet.controller;

import com.example.payment.paymet.dto.DecryptRequest;
import com.example.payment.paymet.dto.DecryptResponse;
import com.example.payment.paymet.dto.EncryptRequest;
import com.example.payment.paymet.dto.EncryptResponse;
import com.example.payment.paymet.service.AesEncryptionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class EncryptionController {

    private final AesEncryptionService aesEncryptionService;

    public EncryptionController(AesEncryptionService aesEncryptionService) {
        this.aesEncryptionService = aesEncryptionService;
    }

    @PostMapping("/encrypt")
    public EncryptResponse encrypt(@RequestBody EncryptRequest request) {
        if (request == null || request.text() == null || request.text().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text is required");
        }
        return new EncryptResponse(aesEncryptionService.encrypt(request.text()));
    }

    @PostMapping("/decrypt")
    public DecryptResponse decrypt(@RequestBody DecryptRequest request) {
        if (request == null || request.cipherText() == null || request.cipherText().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cipherText is required");
        }
        return new DecryptResponse(aesEncryptionService.decrypt(request.cipherText()));
    }
}
