package com.enterprise.superadmin.globaldashboard.service;

import com.enterprise.superadmin.globaldashboard.dto.response.*;
import com.enterprise.superadmin.globaldashboard.dto.response.*;

import java.util.List;


public interface GlobalDashboardService {

    GlobalDashboardResponse getDashboard();
    GlobalDashboardSummaryResponse getSummary();

    GlobalDashboardMetricsResponse getMetrics();

    GlobalDashboardStatusResponse getStatus();
    List<RecentActivityResponse> getRecentActivities();

    List<GlobalDashboardNotificationResponse> getNotifications();
}