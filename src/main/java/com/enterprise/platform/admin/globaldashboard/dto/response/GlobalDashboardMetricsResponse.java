package com.enterprise.platform.admin.globaldashboard.dto.response;

public record GlobalDashboardMetricsResponse(
        double cpuUtilizationPercentage,
        double memoryUsagePercentage,
        double storageUtilizationPercentage,
        long apiRequests,
        long backgroundJobs,
        long failedJobs
) {
}