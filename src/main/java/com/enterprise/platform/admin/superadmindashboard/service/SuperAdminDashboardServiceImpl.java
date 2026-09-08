package com.enterprise.platform.admin.superadmindashboard.service;
import com.enterprise.platform.admin.superadmindashboard.client.PlatformHealthClient;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class SuperAdminDashboardServiceImpl implements SuperAdminDashboardService {
    private final PlatformHealthClient platformHealthClient;
    private final ObjectMapper objectMapper;
    public SuperAdminDashboardServiceImpl( PlatformHealthClient platformHealthClient, ObjectMapper objectMapper) {
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
        String healthResponse = platformHealthClient.getPlatformHealth();
        String platformHealth = "UNKNOWN";
        try {
            JsonNode healthJson = objectMapper.readTree(healthResponse);
            platformHealth = healthJson.path("status").asText("UNKNOWN");
        }
        catch (Exception e) {
            platformHealth = "UNKNOWN";
        }
        return new SuperAdminDashboardResponse(
                25L, 450L, 18L, platformHealth, 127L, 68.5,
                List.of( "License renewal due for Tenant ABC", "High storage utilization detected" ),
                List.of( "Tenant ABC created", "Platform configuration updated", "New Super Admin user added" ) );
    }
}