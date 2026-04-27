package com.example.payment.paymet.service;

import com.example.payment.paymet.config.AuthServiceProperties;
import com.example.payment.paymet.dto.AuthValidateTokenRequest;
import com.example.payment.paymet.dto.AuthValidateTokenResponse;
import java.net.URI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceClient {

    private final RestTemplate authRestTemplate;
    private final AuthServiceProperties properties;

    public AuthServiceClient(@Qualifier("authRestTemplate") RestTemplate authRestTemplate, AuthServiceProperties properties) {
        this.authRestTemplate = authRestTemplate;
        this.properties = properties;
    }

    public boolean validateToken(String token) {
        URI uri = URI.create(trimTrailingSlash(properties.getBaseUrl()) + "/auth/validate");
        try {
            AuthValidateTokenResponse response = authRestTemplate.postForObject(
                uri,
                new AuthValidateTokenRequest(token),
                AuthValidateTokenResponse.class
            );
            return response != null && response.valid();
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token", ex);
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Auth service unavailable", ex);
        }
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
