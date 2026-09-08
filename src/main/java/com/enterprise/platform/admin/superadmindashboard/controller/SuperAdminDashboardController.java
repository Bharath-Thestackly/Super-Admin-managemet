package com.enterprise.platform.admin.superadmindashboard.controller;

import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.enterprise.platform.admin.superadmindashboard.service.SuperAdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Super Admin Dashboard", description = "Platform-wide admin dashboard endpoints")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService dashboardService;

    public SuperAdminDashboardController(
            SuperAdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Get full dashboard", description = "Returns the full super admin dashboard payload")
    @Parameter(name = "X-User-Role", description = "Role of the requesting user. Must be SUPER_ADMIN to access this endpoint.", required = true, in = ParameterIn.HEADER)
    public ResponseEntity<SuperAdminDashboardResponse> getDashboard() {

        SuperAdminDashboardResponse response =
                dashboardService.getDashboard();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Returns a summarized view of platform tenants, users, subscriptions, and health")
    @Parameter(name = "X-User-Role", description = "Role of the requesting user. Must be SUPER_ADMIN to access this endpoint.", required = true, in = ParameterIn.HEADER)
    public ResponseEntity<SuperAdminDashboardResponse> getDashboardSummary() {

        SuperAdminDashboardResponse response =
                dashboardService.getDashboardSummary();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get dashboard statistics", description = "Returns platform statistics for the super admin dashboard")
    @Parameter(name = "X-User-Role", description = "Role of the requesting user. Must be SUPER_ADMIN to access this endpoint.", required = true, in = ParameterIn.HEADER)
    public ResponseEntity<SuperAdminDashboardResponse> getDashboardStatistics() {

        SuperAdminDashboardResponse response =
                dashboardService.getDashboardStatistics();

        return ResponseEntity.ok(response);
    }
}