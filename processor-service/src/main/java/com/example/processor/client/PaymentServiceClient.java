package com.example.processor.client;

import com.example.processor.config.PaymentClientProperties;
import com.example.processor.dto.PaymentStatus;
import com.example.processor.dto.UpdatePaymentStatusRequest;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentServiceClient {

    private final RestTemplate paymentServiceRestTemplate;
    private final PaymentClientProperties properties;

    public PaymentServiceClient(RestTemplate paymentServiceRestTemplate, PaymentClientProperties properties) {
        this.paymentServiceRestTemplate = paymentServiceRestTemplate;
        this.properties = properties;
    }

    public void updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        String base = trimTrailingSlash(properties.getBaseUrl());
        URI uri = URI.create(base + "/payments/" + paymentId + "/status");
        paymentServiceRestTemplate.exchange(
            uri,
            HttpMethod.PATCH,
            new HttpEntity<>(new UpdatePaymentStatusRequest(status)),
            Void.class
        );
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
