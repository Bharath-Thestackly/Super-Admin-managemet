package com.enterprise.platform.admin.globaldashboard.service;

import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardMetricsResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardStatusResponse;
import com.enterprise.platform.admin.globaldashboard.dto.response.GlobalDashboardSummaryResponse;
import com.enterprise.platform.admin.globaldashboard.integration.client.PlatformHealthClient;
import com.enterprise.platform.admin.globaldashboard.integration.client.TenantManagementClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class GlobalDashboardServiceImpl implements GlobalDashboardService {

    private final TenantManagementClient tenantManagementClient;


    private final PlatformHealthClient platformHealthClient;

    public GlobalDashboardServiceImpl(
            TenantManagementClient tenantManagementClient,
            PlatformHealthClient platformHealthClient) {

        this.tenantManagementClient = tenantManagementClient;
        this.platformHealthClient = platformHealthClient;
    }
    @Override
    public GlobalDashboardResponse getDashboard() {

        var tenantSummary = tenantManagementClient.getTenantSummary();
        var platformHealth = platformHealthClient.getPlatformHealth();

        var summary = new GlobalDashboardSummaryResponse(
                tenantSummary.totalTenants(),
                tenantSummary.activeTenants(),
                0,
                0,
                0
        );

        var metrics = new GlobalDashboardMetricsResponse(
                platformHealth.cpuUtilizationPercentage(),
                platformHealth.memoryUsagePercentage(),
                platformHealth.storageUtilizationPercentage(),
                platformHealth.apiRequests(),
                platformHealth.backgroundJobs(),
                platformHealth.failedJobs()
        );

        var status = new GlobalDashboardStatusResponse(
                "HEALTHY",
                5,
                0,
                0
        );

        return new GlobalDashboardResponse(
                summary,
                metrics,
                status
        );
    }
    @Override
    public GlobalDashboardSummaryResponse getSummary() {

        try {
            var tenantSummary = tenantManagementClient.getTenantSummary();

            return new GlobalDashboardSummaryResponse(
                    tenantSummary.totalTenants(),
                    tenantSummary.activeTenants(),
                    0,
                    0,
                    0
            );

        } catch (RestClientException ex) {
            return new GlobalDashboardSummaryResponse(
                    0,
                    0,
                    0,
                    0,
                    0
            );
        }
    }
    @Override
    public GlobalDashboardMetricsResponse getMetrics() {

        try {
            var platformHealth = platformHealthClient.getPlatformHealth();

            return new GlobalDashboardMetricsResponse(
                    platformHealth.cpuUtilizationPercentage(),
                    platformHealth.memoryUsagePercentage(),
                    platformHealth.storageUtilizationPercentage(),
                    platformHealth.apiRequests(),
                    platformHealth.backgroundJobs(),
                    platformHealth.failedJobs()
            );

        } catch (RestClientException ex) {
            return new GlobalDashboardMetricsResponse(
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            );
        }
    }
    @Override
    public GlobalDashboardStatusResponse getStatus() {

        return new GlobalDashboardStatusResponse(
                "HEALTHY",
                5,
                0,
                0
        );
    }

}