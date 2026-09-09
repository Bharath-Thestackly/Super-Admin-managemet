package com.enterprise.platform.admin.superadmindashboard.controller;

import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.enterprise.platform.admin.superadmindashboard.service.SuperAdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Super Admin Dashboard", description = "Consolidated Platform Administration metrics, summary, and statistics APIs")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService dashboardService;

    public SuperAdminDashboardController(SuperAdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get Consolidated Super Admin Dashboard", description = "Aggregates tenant, user, subscription, and health metrics into a consolidated response.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires Super Admin privileges")
    })
    @GetMapping
    public ResponseEntity<SuperAdminDashboardResponse> getDashboard() {
        SuperAdminDashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Dashboard Summary", description = "Returns high-level platform summary counts required by UI widgets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard summary"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse response = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Dashboard Statistics", description = "Returns platform dashboard statistics required by the approved FRS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard statistics"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsResponse> getDashboardStatistics() {
        DashboardStatisticsResponse response = dashboardService.getDashboardStatistics();
        return ResponseEntity.ok(response);
    }
}
