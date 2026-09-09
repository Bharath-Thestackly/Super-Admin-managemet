package com.enterprise.superadmin.globaldashboard;

import com.enterprise.superadmin.globaldashboard.dto.response.GlobalDashboardResponse;
import com.enterprise.superadmin.globaldashboard.integration.client.LicenseManagementClient;
import com.enterprise.superadmin.globaldashboard.integration.client.PlatformHealthClient;
import com.enterprise.superadmin.globaldashboard.integration.client.TenantManagementClient;
import com.enterprise.superadmin.globaldashboard.integration.client.UserManagementClient;
import com.enterprise.superadmin.globaldashboard.integration.dto.LicenseSummaryResponse;
import com.enterprise.superadmin.globaldashboard.integration.dto.PlatformHealthResponse;
import com.enterprise.superadmin.globaldashboard.integration.dto.TenantSummaryResponse;
import com.enterprise.superadmin.globaldashboard.integration.dto.UserSummaryResponse;
import com.enterprise.superadmin.globaldashboard.service.GlobalDashboardServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class GlobalDashboardIntegrationTest {

    @Test
    void globalDashboard_shouldAggregateDataFromAllIntegrationClients() {

        TenantManagementClient tenantClient =
                mock(TenantManagementClient.class);

        PlatformHealthClient healthClient =
                mock(PlatformHealthClient.class);

        LicenseManagementClient licenseClient =
                mock(LicenseManagementClient.class);

        UserManagementClient userClient =
                mock(UserManagementClient.class);

        when(tenantClient.getTenantSummary())
                .thenReturn(new TenantSummaryResponse(10, 8));

        when(healthClient.getPlatformHealth())
                .thenReturn(new PlatformHealthResponse(
                        35.5,
                        48.2,
                        62.0,
                        12500,
                        320,
                        7
                ));

        when(licenseClient.getLicenseSummary())
                .thenReturn(new LicenseSummaryResponse(100));

        when(userClient.getUserSummary())
                .thenReturn(new UserSummaryResponse(
                        1250,
                        1120,
                        87
                ));

        GlobalDashboardServiceImpl service =
                new GlobalDashboardServiceImpl(
                        tenantClient,
                        healthClient,
                        licenseClient,
                        userClient
                );

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

        // Verify integration clients were called
        verify(tenantClient).getTenantSummary();
        verify(healthClient).getPlatformHealth();
        verify(licenseClient).getLicenseSummary();
        verify(userClient).getUserSummary();
    }
}