package com.enterprise.platform.admin.superadmindashboard.service;

import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;

public interface SuperAdminDashboardService {

    /**
     * Aggregates full Super Admin Dashboard data across all domain clients.
     */
    SuperAdminDashboardResponse getDashboard();

    /**
     * Returns high-level platform summary counts required by UI widgets.
     */
    DashboardSummaryResponse getDashboardSummary();

    /**
     * Returns platform dashboard statistics required by the approved FRS.
     */
    DashboardStatisticsResponse getDashboardStatistics();
}