package com.enterprise.platform.admin.globaldashboard.service;

import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardMetricsResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardStatusResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardSummaryResponse;

public interface GlobalDashboardService {

    GlobalDashboardResponse getDashboard();
    GlobalDashboardSummaryResponse getSummary();

    GlobalDashboardMetricsResponse getMetrics();

    GlobalDashboardStatusResponse getStatus();
}