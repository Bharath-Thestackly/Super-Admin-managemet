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
public class PlatformHealthClient {

    private static final Logger log = LoggerFactory.getLogger(PlatformHealthClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String healthUrl;

    public PlatformHealthClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${services.health-service.url:http://localhost:8081/actuator/health}") String healthUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.healthUrl = healthUrl;
    }

    public String getPlatformHealth() {
        try {
            String response = restTemplate.getForObject(healthUrl, String.class);
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                return root.path("status").asText("UNKNOWN");
            }
        } catch (RestClientException e) {
            log.warn("Platform health check failed: {}. Falling back to UNKNOWN.", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to parse health check response: {}", e.getMessage());
        }
        return "UNKNOWN";
    }

    public Optional<Double> getStorageUtilization() {
        try {
            String response = restTemplate.getForObject(healthUrl, String.class);
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode diskDetails = root.path("components").path("diskSpace").path("details");
                if (diskDetails.has("total") && diskDetails.has("free")) {
                    long total = diskDetails.path("total").asLong();
                    long free = diskDetails.path("free").asLong();
                    if (total > 0) {
                        double usedPercent = ((double) (total - free) / total) * 100.0;
                        return Optional.of(Math.round(usedPercent * 10.0) / 10.0);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not determine exact storage utilization from actuator: {}", e.getMessage());
        }
        log.warn("Disk space details unavailable from actuator health endpoint. Returning empty storage utilization.");
        return Optional.empty(); // Do not fabricate metrics when actuator disk details are absent
    }
}
