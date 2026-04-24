package com.example.payment.paymet.service;

import com.example.payment.paymet.util.AesEncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AesEncryptionService {

    private final AesEncryptionUtil aesEncryptionUtil;

    public AesEncryptionService(@Value("${payment.encryption.secret}") String secret) {
        this.aesEncryptionUtil = AesEncryptionUtil.fromUtf8Secret(secret);
    }

    public String encrypt(String plainText) {
        return aesEncryptionUtil.encryptToBase64(plainText);
    }

    public String decrypt(String cipherTextBase64) {
        return aesEncryptionUtil.decryptFromBase64(cipherTextBase64);
    }
}
