package com.enterprise.superadmin.platform_health_service.service;

import com.enterprise.superadmin.platform_health_service.HealthStatus;
import com.enterprise.superadmin.platform_health_service.dto.response.ServiceHealthResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformHealthService {

    private final ServiceHealthService serviceHealthService;
    private final HealthAggregationService healthAggregationService;

    public PlatformHealthService(
            ServiceHealthService serviceHealthService,
            HealthAggregationService healthAggregationService) {

        this.serviceHealthService = serviceHealthService;
        this.healthAggregationService = healthAggregationService;
    }

    public HealthStatus determinePlatformHealth(
            List<ServiceHealthResponse> healthResponses) {

        List<ServiceHealthResponse> validatedResponses =
                serviceHealthService.validateHealthResponses(healthResponses);

        List<HealthStatus> statuses = validatedResponses.stream()
                .map(response ->
                        HealthStatus.valueOf(
                                response.getStatus().trim().toUpperCase()))
                .toList();

        return healthAggregationService.determineOverallStatus(statuses);
    }
}