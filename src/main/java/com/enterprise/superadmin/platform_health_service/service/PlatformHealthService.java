package com.enterprise.superadmin.platform_health_service.service;

import com.enterprise.superadmin.platform_health_service.dto.response.PlatformHealthResponse;
import com.enterprise.superadmin.platform_health_service.dto.response.ServiceHealthResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlatformHealthService {

    public PlatformHealthResponse getPlatformHealth() {

        List<ServiceHealthResponse> services =
                getAllServicesHealth();

        boolean availability = services.stream()
                .allMatch(ServiceHealthResponse::isAvailability);

        String status;

        if (services.isEmpty()) {
            status = "FAILED";
            availability = false;
        } else if (availability) {
            status = "HEALTHY";
        } else {
            status = "DEGRADED";
        }

        return PlatformHealthResponse.builder()
                .status(status)
                .availability(availability)
                .retrievedAt(LocalDateTime.now())
                .services(services)
                .build();
    }

    public List<ServiceHealthResponse> getAllServicesHealth() {

        return new ArrayList<>();
    }

    public ServiceHealthResponse getServiceHealth(String serviceName) {

        return ServiceHealthResponse.builder()
                .serviceName(serviceName)
                .status("UNKNOWN")
                .availability(false)
                .responseTimeMs(null)
                .build();
    }
}