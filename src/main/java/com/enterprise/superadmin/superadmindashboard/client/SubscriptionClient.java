package com.enterprise.superadmin.superadmindashboard.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SubscriptionClient {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String subscriptionServiceUrl;
    private final String fallbackUrl;

    public SubscriptionClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${services.subscription-service.url:http://subscription-service}") String subscriptionServiceUrl,
            @Value("${services.subscription-service.fallback-url:http://localhost:8084}") String fallbackUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.subscriptionServiceUrl = subscriptionServiceUrl;
        this.fallbackUrl = fallbackUrl;
    }

    public Optional<Long> getActiveSubscriptions() {
        String endpoint = fallbackUrl + "/api/v1/subscriptions/count";
        try {
            String json = restTemplate.getForObject(endpoint, String.class);
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                if (root.has("count")) {
                    return Optional.of(root.get("count").asLong());
                } else if (root.isNumber()) {
                    return Optional.of(root.asLong());
                }
            }
        } catch (RestClientException e) {
            log.warn("Subscription service unreachable at {}: {}. Will use graceful fallback.", endpoint, e.getMessage());
        } catch (Exception e) {
            log.error("Error parsing Subscription service response: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public List<String> getSubscriptionAlerts() {
        String endpoint = fallbackUrl + "/api/v1/subscriptions/alerts";
        List<String> alerts = new ArrayList<>();
        try {
            String json = restTemplate.getForObject(endpoint, String.class);
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                if (root.isArray()) {
                    for (JsonNode item : root) {
                        alerts.add(item.asText());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Subscription alerts unavailable: {}", e.getMessage());
        }
        return alerts;
    }
}