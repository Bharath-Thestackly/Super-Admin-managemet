package com.enterprise.platform.admin.superadmindashboard.client;

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
public class TenantClient {

    private static final Logger log = LoggerFactory.getLogger(TenantClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String tenantServiceUrl;
    private final String fallbackUrl;

    public TenantClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${services.tenant-service.url:http://tenant-service}") String tenantServiceUrl,
            @Value("${services.tenant-service.fallback-url:http://localhost:8082}") String fallbackUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tenantServiceUrl = tenantServiceUrl;
        this.fallbackUrl = fallbackUrl;
    }

    public Optional<Long> getTotalTenants() {
        String endpoint = fallbackUrl + "/api/v1/tenants/count";
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
            log.warn("Tenant service unreachable at {}: {}. Will use graceful fallback.", endpoint, e.getMessage());
        } catch (Exception e) {
            log.error("Error parsing Tenant service response: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public List<String> getRecentTenantActivities() {
        String endpoint = fallbackUrl + "/api/v1/tenants/recent";
        List<String> activities = new ArrayList<>();
        try {
            String json = restTemplate.getForObject(endpoint, String.class);
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                if (root.isArray()) {
                    for (JsonNode item : root) {
                        activities.add(item.asText());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Tenant activity feed unavailable: {}", e.getMessage());
        }
        return activities;
    }
}