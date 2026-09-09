package com.enterprise.superadmin.globaldashboard;

import com.enterprise.superadmin.globaldashboard.dto.response.GlobalDashboardMetricsResponse;
import com.enterprise.superadmin.globaldashboard.dto.response.GlobalDashboardResponse;
import com.enterprise.superadmin.globaldashboard.dto.response.GlobalDashboardStatusResponse;
import com.enterprise.superadmin.globaldashboard.dto.response.GlobalDashboardSummaryResponse;
import com.enterprise.superadmin.globaldashboard.integration.client.LicenseManagementClient;
import com.enterprise.superadmin.globaldashboard.integration.client.PlatformHealthClient;
import com.enterprise.superadmin.globaldashboard.integration.client.TenantManagementClient;
import com.enterprise.superadmin.globaldashboard.integration.client.UserManagementClient;
import com.enterprise.superadmin.globaldashboard.integration.dto.LicenseSummaryResponse;
import com.enterprise.superadmin.globaldashboard.integration.dto.PlatformHealthResponse;
import com.enterprise.superadmin.globaldashboard.integration.dto.TenantSummaryResponse;
import com.enterprise.superadmin.globaldashboard.integration.dto.UserSummaryResponse;
import com.enterprise.superadmin.globaldashboard.service.GlobalDashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalDashboardServiceImplTest {

    private TenantManagementClient tenantManagementClient;
    private PlatformHealthClient platformHealthClient;
    private LicenseManagementClient licenseManagementClient;
    private UserManagementClient userManagementClient;

    private GlobalDashboardServiceImpl service;

    @BeforeEach
    void setUp() {

        tenantManagementClient = mock(TenantManagementClient.class);
        platformHealthClient = mock(PlatformHealthClient.class);
        licenseManagementClient = mock(LicenseManagementClient.class);
        userManagementClient = mock(UserManagementClient.class);

        service = new GlobalDashboardServiceImpl(
                tenantManagementClient,
                platformHealthClient,
                licenseManagementClient,
                userManagementClient
        );
    }

    @Test
    void getDashboard_shouldReturnCompleteDashboard() {

        when(tenantManagementClient.getTenantSummary())
                .thenReturn(new TenantSummaryResponse(
                        10,
                        8
                ));

        when(platformHealthClient.getPlatformHealth())
                .thenReturn(new PlatformHealthResponse(
                        35.5,
                        48.2,
                        62.0,
                        12500,
                        320,
                        7
                ));

        when(licenseManagementClient.getLicenseSummary())
                .thenReturn(new LicenseSummaryResponse(
                        100
                ));

        when(userManagementClient.getUserSummary())
                .thenReturn(new UserSummaryResponse(
                        1250,
                        1120,
                        87
                ));

        GlobalDashboardResponse response =
                service.getDashboard();

        assertNotNull(response);

        // Summary
        assertEquals(
                10,
                response.summary().totalTenants()
        );

        assertEquals(
                8,
                response.summary().activeTenants()
        );

        assertEquals(
                1250,
                response.summary().totalUsers()
        );

        assertEquals(
                1120,
                response.summary().activeUsers()
        );

        assertEquals(
                87,
                response.summary().onlineUsers()
        );

        // Metrics
        assertEquals(
                35.5,
                response.metrics().cpuUtilizationPercentage()
        );

        assertEquals(
                48.2,
                response.metrics().memoryUsagePercentage()
        );

        assertEquals(
                62.0,
                response.metrics().storageUtilizationPercentage()
        );

        assertEquals(
                12500,
                response.metrics().apiRequests()
        );

        assertEquals(
                320,
                response.metrics().backgroundJobs()
        );

        assertEquals(
                7,
                response.metrics().failedJobs()
        );

        assertEquals(
                100,
                response.metrics().activeLicenses()
        );

        // Status
        assertEquals(
                "HEALTHY",
                response.status().overallStatus()
        );

        assertEquals(
                5,
                response.status().healthyServices()
        );

        assertEquals(
                2,
                response.status().degradedServices()
        );

        assertEquals(
                1,
                response.status().unavailableServices()
        );
    }

    @Test
    void getSummary_shouldReturnTenantAndUserSummary() {

        when(tenantManagementClient.getTenantSummary())
                .thenReturn(new TenantSummaryResponse(
                        10,
                        8
                ));

        when(userManagementClient.getUserSummary())
                .thenReturn(new UserSummaryResponse(
                        1250,
                        1120,
                        87
                ));

        GlobalDashboardSummaryResponse response =
                service.getSummary();

        assertNotNull(response);

        assertEquals(
                10,
                response.totalTenants()
        );

        assertEquals(
                8,
                response.activeTenants()
        );

        assertEquals(
                1250,
                response.totalUsers()
        );

        assertEquals(
                1120,
                response.activeUsers()
        );

        assertEquals(
                87,
                response.onlineUsers()
        );
    }

    @Test
    void getMetrics_shouldReturnPlatformAndLicenseMetrics() {

        when(platformHealthClient.getPlatformHealth())
                .thenReturn(new PlatformHealthResponse(
                        35.5,
                        48.2,
                        62.0,
                        12500,
                        320,
                        7
                ));

        when(licenseManagementClient.getLicenseSummary())
                .thenReturn(new LicenseSummaryResponse(
                        100
                ));

        GlobalDashboardMetricsResponse response =
                service.getMetrics();

        assertNotNull(response);

        assertEquals(
                35.5,
                response.cpuUtilizationPercentage()
        );

        assertEquals(
                48.2,
                response.memoryUsagePercentage()
        );

        assertEquals(
                62.0,
                response.storageUtilizationPercentage()
        );

        assertEquals(
                12500,
                response.apiRequests()
        );

        assertEquals(
                320,
                response.backgroundJobs()
        );

        assertEquals(
                7,
                response.failedJobs()
        );

        assertEquals(
                100,
                response.activeLicenses()
        );
    }

    @Test
    void getStatus_shouldReturnHealthyStatus() {

        GlobalDashboardStatusResponse response =
                service.getStatus();

        assertNotNull(response);

        assertEquals(
                "HEALTHY",
                response.overallStatus()
        );

        assertEquals(
                5,
                response.healthyServices()
        );

        assertEquals(
                2,
                response.degradedServices()
        );

        assertEquals(
                1,
                response.unavailableServices()
        );
    }
}