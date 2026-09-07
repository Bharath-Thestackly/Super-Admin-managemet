package com.enterprise.platform.admin.globaldashboard.dto.response;

public record GlobalDashboardStatusResponse(
        String overallStatus,
        long healthyServices,
        long degradedServices,
        long unavailableServices
) {
}