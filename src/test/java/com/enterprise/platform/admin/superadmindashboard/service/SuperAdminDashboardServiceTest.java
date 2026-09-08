package com.enterprise.platform.admin.superadmindashboard.service;

import com.enterprise.platform.admin.superadmindashboard.client.PlatformHealthClient;
import com.enterprise.platform.admin.superadmindashboard.client.SubscriptionClient;
import com.enterprise.platform.admin.superadmindashboard.client.TenantClient;
import com.enterprise.platform.admin.superadmindashboard.client.UserClient;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import org.junit.jupiter.api.BeforeEach;
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
    private PlatformHealthClient platformHealthClient;

    @Mock
    private TenantClient tenantClient;

    @Mock
    private UserClient userClient;

    @Mock
    private SubscriptionClient subscriptionClient;

    @InjectMocks
    private SuperAdminDashboardServiceImpl dashboardService;

    @BeforeEach
    void setup() {
        when(platformHealthClient.getPlatformHealth()).thenReturn("UP");
        when(platformHealthClient.getStorageUtilization()).thenReturn(Optional.of(55.0));
    }

    @Test
    @DisplayName("Full aggregation succeeds with all downstream clients healthy")
    void testFullAggregationSuccess() {
        when(tenantClient.getTotalTenants()).thenReturn(Optional.of(30L));
        when(tenantClient.getRecentTenantActivities()).thenReturn(List.of("Tenant Org1 created"));
        when(userClient.getTotalUsers()).thenReturn(Optional.of(500L));
        when(userClient.getActiveSessions()).thenReturn(Optional.of(120L));
        when(subscriptionClient.getActiveSubscriptions()).thenReturn(Optional.of(28L));
        when(subscriptionClient.getSubscriptionAlerts()).thenReturn(List.of("License renewal due"));

        SuperAdminDashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(30L, response.getTotalTenants());
        assertEquals(500L, response.getTotalUsers());
        assertEquals(28L, response.getActiveSubscriptions());
        assertEquals("UP", response.getPlatformHealthStatus());
        assertEquals(120L, response.getActiveSessions());
        assertEquals(55.0, response.getStorageUtilization());
        assertTrue(response.getSystemAlerts().contains("License renewal due"));
        assertTrue(response.getRecentActivities().contains("Tenant Org1 created"));
    }

    @Test
    @DisplayName("Partial failure resilience: Tenant & User services down, graceful degradation with alerts")
    void testPartialDependencyFailureDegradation() {
        when(tenantClient.getTotalTenants()).thenReturn(Optional.empty()); // Service down
        when(tenantClient.getRecentTenantActivities()).thenReturn(List.of());
        when(userClient.getTotalUsers()).thenReturn(Optional.empty()); // Service down
        when(userClient.getActiveSessions()).thenReturn(Optional.empty());
        when(subscriptionClient.getActiveSubscriptions()).thenReturn(Optional.of(15L));
        when(subscriptionClient.getSubscriptionAlerts()).thenReturn(List.of());

        SuperAdminDashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(0L, response.getTotalTenants());
        assertEquals(0L, response.getTotalUsers());
        assertEquals(15L, response.getActiveSubscriptions());
        assertEquals("UP", response.getPlatformHealthStatus());

        // Alerts must contain dependency warnings instead of blowing up with 500
        assertTrue(response.getSystemAlerts().stream()
                .anyMatch(alert -> alert.contains("Tenant Service is currently unavailable")));
        assertTrue(response.getSystemAlerts().stream()
                .anyMatch(alert -> alert.contains("User Service is currently unavailable")));
    }

    @Test
    @DisplayName("Summary response extracts only summary counts")
    void testSummaryMapping() {
        when(tenantClient.getTotalTenants()).thenReturn(Optional.of(10L));
        when(userClient.getTotalUsers()).thenReturn(Optional.of(100L));
        when(userClient.getActiveSessions()).thenReturn(Optional.of(20L));
        when(subscriptionClient.getActiveSubscriptions()).thenReturn(Optional.of(8L));

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();

        assertEquals(10L, summary.getTotalTenants());
        assertEquals(100L, summary.getTotalUsers());
        assertEquals(8L, summary.getActiveSubscriptions());
        assertEquals("UP", summary.getPlatformHealthStatus());
    }

    @Test
    @DisplayName("Statistics response formats metrics and alert count")
    void testStatisticsMapping() {
        when(tenantClient.getTotalTenants()).thenReturn(Optional.of(10L));
        when(userClient.getTotalUsers()).thenReturn(Optional.of(100L));
        when(userClient.getActiveSessions()).thenReturn(Optional.of(25L));
        when(subscriptionClient.getActiveSubscriptions()).thenReturn(Optional.of(8L));
        when(subscriptionClient.getSubscriptionAlerts()).thenReturn(List.of("Alert 1", "Alert 2"));

        DashboardStatisticsResponse stats = dashboardService.getDashboardStatistics();

        assertEquals(10L, stats.getTotalTenants());
        assertEquals(25L, stats.getActiveSessions());
        assertEquals(55.0, stats.getStorageUtilization());
        assertEquals(2L, stats.getSystemAlertsCount());
    }
}