package com.enterprise.superadmin.superadmindashboard.client;

import java.util.List;

public record GlobalDashboardContractResponse(
        Summary summary,
        Metrics metrics,
        Status status,
        List<RecentActivity> recentActivities,
        List<Notification> notifications) {

    public record Summary(
            Long totalTenants,
            Long activeTenants,
            Long totalUsers,
            Long activeUsers,
            Long onlineUsers) {}

    public record Metrics(
            Double cpuUtilizationPercentage,
            Double memoryUsagePercentage,
            Double storageUtilizationPercentage,
            Long apiRequests,
            Long backgroundJobs,
            Long failedJobs,
            Long activeLicenses) {}

    public record Status(
            String overallStatus,
            Long healthyServices,
            Long degradedServices,
            Long unavailableServices) {}

    public record RecentActivity(
            String activityId,
            String description,
            String performedBy,
            String moduleName,
            String activityDateTime) {}

    public record Notification(
            String notificationId,
            String title,
            String message,
            String severity,
            String createdAt) {}
}
