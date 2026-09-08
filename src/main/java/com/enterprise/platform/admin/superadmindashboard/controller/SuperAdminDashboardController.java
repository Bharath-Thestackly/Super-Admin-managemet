package com.enterprise.platform.admin.superadmindashboard.controller;

import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.enterprise.platform.admin.superadmindashboard.service.SuperAdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService dashboardService;

    public SuperAdminDashboardController(
            SuperAdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<SuperAdminDashboardResponse> getDashboard() {

        SuperAdminDashboardResponse response =
                dashboardService.getDashboard();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<SuperAdminDashboardResponse> getDashboardSummary() {

        SuperAdminDashboardResponse response =
                dashboardService.getDashboardSummary();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<SuperAdminDashboardResponse> getDashboardStatistics() {

        SuperAdminDashboardResponse response =
                dashboardService.getDashboardStatistics();

        return ResponseEntity.ok(response);
    }
}