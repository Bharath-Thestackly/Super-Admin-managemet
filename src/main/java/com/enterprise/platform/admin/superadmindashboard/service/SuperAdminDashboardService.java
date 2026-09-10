package com.enterprise.platform.admin.superadmindashboard.service;

import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardNavigationResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;

public interface SuperAdminDashboardService {

    SuperAdminDashboardResponse getDashboard();

    DashboardSummaryResponse getDashboardSummary();

    DashboardStatisticsResponse getDashboardStatistics();

    DashboardNavigationResponse getDashboardNavigation();
}
