package com.enterprise.platform.admin.superadmindashboard.service;

import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;

public interface SuperAdminDashboardService {
    SuperAdminDashboardResponse getDashboard();
    SuperAdminDashboardResponse getDashboardSummary();

    SuperAdminDashboardResponse getDashboardStatistics();
}
