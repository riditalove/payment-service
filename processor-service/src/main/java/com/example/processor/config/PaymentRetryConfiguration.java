package com.example.processor.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;

@Configuration
public class PaymentRetryConfiguration {

    @Bean
    public RetryTemplate paymentRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> retryable = new HashMap<>();
        retryable.put(RestClientException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(4, retryable, true);
        template.setRetryPolicy(retryPolicy);

        ExponentialBackOffPolicy backoff = new ExponentialBackOffPolicy();
        backoff.setInitialInterval(1000L);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(10_000L);
        template.setBackOffPolicy(backoff);

        return template;
    }
}
