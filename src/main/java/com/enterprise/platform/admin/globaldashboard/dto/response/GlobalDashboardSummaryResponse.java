package com.enterprise.platform.admin.globaldashboard.dto.response;

public record GlobalDashboardSummaryResponse(
        long totalTenants,
        long activeTenants,
        long totalOrganizations,
        long activeUsers,
        long onlineUsers
) {
}