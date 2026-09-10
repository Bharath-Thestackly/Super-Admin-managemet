package com.enterprise.superadmin.superadmindashboard.service;

import com.enterprise.superadmin.superadmindashboard.dto.response.DashboardNavigationResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.SuperAdminDashboardResponse;

public interface SuperAdminDashboardService {

    SuperAdminDashboardResponse getDashboard();

    DashboardSummaryResponse getDashboardSummary();

    DashboardStatisticsResponse getDashboardStatistics();

    DashboardNavigationResponse getDashboardNavigation();
}
