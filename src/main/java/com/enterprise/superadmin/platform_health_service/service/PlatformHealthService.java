package com.enterprise.superadmin.platform_health_service.service;

import com.enterprise.superadmin.platform_health_service.HealthStatus;
import com.enterprise.superadmin.platform_health_service.dto.response.PlatformHealthResponse;
import com.enterprise.superadmin.platform_health_service.dto.response.ServiceHealthResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public PlatformHealthResponse getPlatformHealth() {

        List<ServiceHealthResponse> services =
                getAllServicesHealth();

        if (services == null || services.isEmpty()) {
            return PlatformHealthResponse.builder()
                    .status(HealthStatus.DEGRADED.name())
                    .availability(false)
                    .retrievedAt(LocalDateTime.now())
                    .services(services)
                    .build();
        }

        List<ServiceHealthResponse> validatedServices =
                serviceHealthService.validateHealthResponses(services);

        List<HealthStatus> statuses = validatedServices.stream()
                .map(service ->
                        HealthStatus.valueOf(
                                service.getStatus().trim().toUpperCase()))
                .toList();

        HealthStatus overallStatus =
                healthAggregationService.determineOverallStatus(statuses);

        boolean availability =
                overallStatus == HealthStatus.HEALTHY;

        return PlatformHealthResponse.builder()
                .status(overallStatus.name())
                .availability(availability)
                .retrievedAt(LocalDateTime.now())
                .services(validatedServices)
                .build();
    }

    public List<ServiceHealthResponse> getAllServicesHealth() {
        return List.of();
    }

    public ServiceHealthResponse getServiceHealth(String serviceName) {

        return ServiceHealthResponse.builder()
                .serviceName(serviceName)
                .status(HealthStatus.DEGRADED.name())
                .availability(false)
                .responseTimeMs(null)
                .build();
    }
}