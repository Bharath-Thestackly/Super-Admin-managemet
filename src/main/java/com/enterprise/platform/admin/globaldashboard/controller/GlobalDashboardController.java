package com.enterprise.platform.admin.globaldashboard.controller;

import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardMetricsResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardStatusResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardSummaryResponse;
import com.enterprise.platform.admin.globaldashboard.service.GlobalDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/global-dashboard")
public class GlobalDashboardController {

    private final GlobalDashboardService globalDashboardService;

    public GlobalDashboardController(GlobalDashboardService globalDashboardService) {
        this.globalDashboardService = globalDashboardService;
    }

    @GetMapping
    public GlobalDashboardResponse getDashboard() {
        return globalDashboardService.getDashboard();
    }
    @GetMapping("/summary")
    public GlobalDashboardSummaryResponse getSummary() {
        return globalDashboardService.getSummary();
    }

    @GetMapping("/metrics")
    public GlobalDashboardMetricsResponse getMetrics() {
        return globalDashboardService.getMetrics();
    }

    @GetMapping("/status")
    public GlobalDashboardStatusResponse getStatus() {
        return globalDashboardService.getStatus();
    }
}