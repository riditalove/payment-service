package com.example.payment.paymet.service;

import com.example.payment.paymet.config.EncryptionServiceProperties;
import com.example.payment.paymet.dto.EncryptRequest;
import com.example.payment.paymet.dto.EncryptResponse;
import java.net.URI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EncryptionServiceClient {

    private final RestTemplate encryptionRestTemplate;
    private final EncryptionServiceProperties properties;

    public EncryptionServiceClient(
        @Qualifier("encryptionRestTemplate") RestTemplate encryptionRestTemplate,
        EncryptionServiceProperties properties
    ) {
        this.encryptionRestTemplate = encryptionRestTemplate;
        this.properties = properties;
    }

    /**
     * Calls the remote encryption service POST /encrypt before persisting sensitive card data.
     */
    public String encrypt(String plainText) {
        URI uri = URI.create(trimTrailingSlash(properties.getBaseUrl()) + "/encrypt");
        try {
            EncryptResponse response = encryptionRestTemplate.postForObject(
                uri,
                new EncryptRequest(plainText),
                EncryptResponse.class
            );
            if (response == null || response.cipherText() == null || response.cipherText().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Encryption service returned an empty cipherText"
                );
            }
            return response.cipherText();
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Encryption service error: " + ex.getStatusCode(),
                ex
            );
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Encryption service unavailable",
                ex
            );
        }
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
