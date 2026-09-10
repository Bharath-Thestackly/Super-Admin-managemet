package com.enterprise.superadmin.superadmindashboard.service;

import com.enterprise.superadmin.superadmindashboard.client.DashboardContractClient;
import com.enterprise.superadmin.superadmindashboard.client.GlobalDashboardContractResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SuperAdminDashboardServiceTest {

    @Mock
    private DashboardContractClient dashboardContractClient;

    @InjectMocks
    private SuperAdminDashboardServiceImpl dashboardService;

    private GlobalDashboardContractResponse buildContract() {
        return new GlobalDashboardContractResponse(
                new GlobalDashboardContractResponse.Summary(30L, 25L, 500L, 480L, 120L),
                new GlobalDashboardContractResponse.Metrics(35.5, 48.2, 55.0, 12500L, 320L, 7L, 28L),
                new GlobalDashboardContractResponse.Status("HEALTHY", 5L, 0L, 0L),
                List.of(new GlobalDashboardContractResponse.RecentActivity(
                        "ACT-001", "Tenant created successfully", "admin@jsuite.com",
                        "Tenant Management", "2026-09-10T14:36:55.5658996")),
                List.of(new GlobalDashboardContractResponse.Notification(
                        "NOT-001", "License Renewal Due", "License renewal due soon", "WARNING",
                        "2026-09-10T14:31:55.5658996"))
        );
    }

    @Test
    @DisplayName("Full aggregation succeeds when global dashboard contract is available")
    void testFullAggregationSuccess() {
        when(dashboardContractClient.getGlobalDashboardContract()).thenReturn(Optional.of(buildContract()));

        SuperAdminDashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(30L, response.getTotalTenants());
        assertEquals(500L, response.getTotalUsers());
        assertEquals(28L, response.getActiveSubscriptions());
        assertEquals("HEALTHY", response.getPlatformHealthStatus());
        assertEquals(120L, response.getActiveSessions());
        assertEquals(55.0, response.getStorageUtilization());
        assertTrue(response.getSystemAlerts().get(0).contains("License Renewal Due"));
        assertTrue(response.getRecentActivities().get(0).contains("Tenant created successfully"));
    }

    @Test
    @DisplayName("Graceful degradation when global dashboard contract is unavailable")
    void testDependencyFailureDegradation() {
        when(dashboardContractClient.getGlobalDashboardContract()).thenReturn(Optional.empty());

        SuperAdminDashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(0L, response.getTotalTenants());
        assertEquals(0L, response.getTotalUsers());
        assertEquals(0L, response.getActiveSubscriptions());
        assertEquals("UNKNOWN", response.getPlatformHealthStatus());
        assertTrue(response.getSystemAlerts().isEmpty());
        assertTrue(response.getRecentActivities().isEmpty());
    }

    @Test
    @DisplayName("Summary response extracts only summary counts")
    void testSummaryMapping() {
        when(dashboardContractClient.getGlobalDashboardContract()).thenReturn(Optional.of(buildContract()));

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();

        assertEquals(30L, summary.getTotalTenants());
        assertEquals(500L, summary.getTotalUsers());
        assertEquals(28L, summary.getActiveSubscriptions());
        assertEquals("HEALTHY", summary.getPlatformHealthStatus());
    }

    @Test
    @DisplayName("Statistics response formats metrics and alert count")
    void testStatisticsMapping() {
        when(dashboardContractClient.getGlobalDashboardContract()).thenReturn(Optional.of(buildContract()));

        DashboardStatisticsResponse stats = dashboardService.getDashboardStatistics();

        assertEquals(30L, stats.getTotalTenants());
        assertEquals(120L, stats.getActiveSessions());
        assertEquals(55.0, stats.getStorageUtilization());
        assertEquals(1L, stats.getSystemAlertsCount());
    }
}
