package com.enterprise.superadmin.globaldashboard.integration.dto;

public record UserSummaryResponse(
        long totalUsers,
        long activeUsers,
        long onlineUsers
) {
}