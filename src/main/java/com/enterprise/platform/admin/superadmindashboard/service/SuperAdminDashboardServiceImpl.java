package com.enterprise.platform.admin.superadmindashboard.service;

import com.enterprise.platform.admin.superadmindashboard.client.PlatformHealthClient;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class SuperAdminDashboardServiceImpl implements SuperAdminDashboardService {

    private final PlatformHealthClient platformHealthClient;
    private final ObjectMapper objectMapper;

    public SuperAdminDashboardServiceImpl(PlatformHealthClient platformHealthClient, ObjectMapper objectMapper) {
        this.platformHealthClient = platformHealthClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public SuperAdminDashboardResponse getDashboard() {
        return createDashboardResponse();
    }

    @Override
    public SuperAdminDashboardResponse getDashboardSummary() {
        return createDashboardResponse();
    }

    @Override
    public SuperAdminDashboardResponse getDashboardStatistics() {
        return createDashboardResponse();
    }

    private SuperAdminDashboardResponse createDashboardResponse() {
        String platformHealth = fetchPlatformHealth();

        return new SuperAdminDashboardResponse(
                25L, 450L, 18L, platformHealth, 127L, 68.5,
                List.of("License renewal due for Tenant ABC", "High storage utilization detected"),
                List.of("Tenant ABC created", "Platform configuration updated", "New Super Admin user added"));
    }

    private String fetchPlatformHealth() {
        try {
            String healthResponse = platformHealthClient.getPlatformHealth();
            JsonNode healthJson = objectMapper.readTree(healthResponse);
            return healthJson.path("status").asText("UNKNOWN");
        } catch (RestClientException e) {
            // Health service timed out, refused connection, or errored -
            // a partial dependency failure should not break the whole dashboard.
            return "UNKNOWN";
        } catch (Exception e) {
            // Any unexpected response format also falls back safely.
            return "UNKNOWN";
        }
    }
}