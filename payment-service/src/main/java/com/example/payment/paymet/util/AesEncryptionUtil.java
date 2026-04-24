package com.example.payment.paymet.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Thread-safe AES-GCM helper: each operation uses a new {@link Cipher} instance.
 * Ciphertext is IV + GCM tag + ciphertext, encoded as Base64 (URL-safe encoding is not used).
 */
public final class AesEncryptionUtil {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int MIN_SECRET_LENGTH = 16;

    private final SecretKeySpec secretKeySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    private AesEncryptionUtil(SecretKeySpec secretKeySpec) {
        this.secretKeySpec = secretKeySpec;
    }

    /**
     * Derives a 256-bit AES key from the UTF-8 secret using SHA-256 (never stores the raw secret string on the util).
     */
    public static AesEncryptionUtil fromUtf8Secret(String secret) {
        Objects.requireNonNull(secret, "secret");
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                "payment.encryption.secret must be at least " + MIN_SECRET_LENGTH + " characters"
            );
        }
        byte[] keyBytes = sha256(secret.getBytes(StandardCharsets.UTF_8));
        return new AesEncryptionUtil(new SecretKeySpec(keyBytes, "AES"));
    }

    public String encryptToBase64(String plainText) {
        Objects.requireNonNull(plainText, "plainText");
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Encryption failed", ex);
        }
    }

    public String decryptFromBase64(String cipherTextBase64) {
        Objects.requireNonNull(cipherTextBase64, "cipherTextBase64");
        try {
            byte[] combined = Base64.getDecoder().decode(cipherTextBase64);
            if (combined.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid ciphertext");
            }
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTES);
            byte[] cipherBytes = Arrays.copyOfRange(combined, IV_LENGTH_BYTES, combined.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException("Decryption failed", ex);
        }
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
