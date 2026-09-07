package com.enterprise.platform.admin.globaldashboard.integration.dto;

public record PlatformHealthResponse(
        double cpuUtilizationPercentage,
        double memoryUsagePercentage,
        double storageUtilizationPercentage,
        long apiRequests,
        long backgroundJobs,
        long failedJobs
) {
}