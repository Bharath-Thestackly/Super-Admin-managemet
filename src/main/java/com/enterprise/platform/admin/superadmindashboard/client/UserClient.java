package com.enterprise.platform.admin.superadmindashboard.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String userServiceUrl;
    private final String fallbackUrl;

    public UserClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${services.user-service.url:http://user-service}") String userServiceUrl,
            @Value("${services.user-service.fallback-url:http://localhost:8083}") String fallbackUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.userServiceUrl = userServiceUrl;
        this.fallbackUrl = fallbackUrl;
    }

    public Optional<Long> getTotalUsers() {
        String endpoint = fallbackUrl + "/api/v1/users/count";
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
            log.warn("User service unreachable at {}: {}. Will use graceful fallback.", endpoint, e.getMessage());
        } catch (Exception e) {
            log.error("Error parsing User service response: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Long> getActiveSessions() {
        String endpoint = fallbackUrl + "/api/v1/users/sessions/active";
        try {
            String json = restTemplate.getForObject(endpoint, String.class);
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                if (root.has("activeSessions")) {
                    return Optional.of(root.get("activeSessions").asLong());
                } else if (root.isNumber()) {
                    return Optional.of(root.asLong());
                }
            }
        } catch (Exception e) {
            log.debug("Active sessions endpoint unavailable: {}", e.getMessage());
        }
        return Optional.empty();
    }
}