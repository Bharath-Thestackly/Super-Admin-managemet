package com.enterprise.platform.admin.globaldashboard.dto.response;

public record GlobalDashboardResponse(
        GlobalDashboardSummaryResponse summary,
        GlobalDashboardMetricsResponse metrics,
        GlobalDashboardStatusResponse status
) {
}