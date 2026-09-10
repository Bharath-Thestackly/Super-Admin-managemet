package com.enterprise.superadmin.superadmindashboard.service;

import com.enterprise.superadmin.superadmindashboard.client.DashboardContractClient;
import com.enterprise.superadmin.superadmindashboard.client.GlobalDashboardContractResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.DashboardNavigationResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.superadmin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminDashboardServiceImpl implements SuperAdminDashboardService {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminDashboardServiceImpl.class);

    private final DashboardContractClient dashboardContractClient;

    public SuperAdminDashboardServiceImpl(DashboardContractClient dashboardContractClient) {
        this.dashboardContractClient = dashboardContractClient;
    }

    @Override
    public SuperAdminDashboardResponse getDashboard() {
        Optional<GlobalDashboardContractResponse> contractOpt = dashboardContractClient.getGlobalDashboardContract();

        if (contractOpt.isEmpty()) {
            log.warn("Global dashboard contract unavailable. Returning empty/default dashboard data.");
            SuperAdminDashboardResponse response = new SuperAdminDashboardResponse(
                    0L, 0L, 0L, "UNKNOWN", 0L, 0.0,
                    new ArrayList<>(), new ArrayList<>()
            );
            response.setTimestamp(Instant.now());
            return response;
        }

        GlobalDashboardContractResponse contract = contractOpt.get();

        Long totalTenants = contract.summary() != null && contract.summary().totalTenants() != null
                ? contract.summary().totalTenants() : 0L;
        Long totalUsers = contract.summary() != null && contract.summary().totalUsers() != null
                ? contract.summary().totalUsers() : 0L;
        Long activeSessions = contract.summary() != null && contract.summary().onlineUsers() != null
                ? contract.summary().onlineUsers() : 0L;

        Long activeSubscriptions = contract.metrics() != null && contract.metrics().activeLicenses() != null
                ? contract.metrics().activeLicenses() : 0L;
        Double storageUtilization = contract.metrics() != null && contract.metrics().storageUtilizationPercentage() != null
                ? contract.metrics().storageUtilizationPercentage() : 0.0;

        String platformHealthStatus = contract.status() != null && contract.status().overallStatus() != null
                ? contract.status().overallStatus() : "UNKNOWN";

        List<String> systemAlerts = new ArrayList<>();
        if (contract.notifications() != null) {
            for (GlobalDashboardContractResponse.Notification notification : contract.notifications()) {
                if (notification.severity() != null
                        && ("WARNING".equalsIgnoreCase(notification.severity())
                            || "ERROR".equalsIgnoreCase(notification.severity()))) {
                    systemAlerts.add(notification.title() + ": " + notification.message());
                }
            }
        }

        List<String> recentActivities = new ArrayList<>();
        if (contract.recentActivities() != null) {
            for (GlobalDashboardContractResponse.RecentActivity activity : contract.recentActivities()) {
                recentActivities.add(activity.moduleName() + ": " + activity.description());
            }
        }

        SuperAdminDashboardResponse response = new SuperAdminDashboardResponse(
                totalTenants,
                totalUsers,
                activeSubscriptions,
                platformHealthStatus,
                activeSessions,
                storageUtilization,
                systemAlerts,
                recentActivities
        );
        response.setTimestamp(Instant.now());
        return response;
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        SuperAdminDashboardResponse full = getDashboard();
        return new DashboardSummaryResponse(
                full.getTotalTenants(),
                full.getTotalUsers(),
                full.getActiveSubscriptions(),
                full.getPlatformHealthStatus()
        );
    }

    @Override
    public DashboardStatisticsResponse getDashboardStatistics() {
        SuperAdminDashboardResponse full = getDashboard();
        return new DashboardStatisticsResponse(
                full.getTotalTenants(),
                full.getTotalUsers(),
                full.getActiveSubscriptions(),
                full.getPlatformHealthStatus(),
                full.getActiveSessions(),
                full.getStorageUtilization(),
                (long) full.getSystemAlerts().size()
        );
    }

    @Override
    public DashboardNavigationResponse getDashboardNavigation() {
        List<DashboardNavigationResponse.NavigationItem> items = List.of(
                new DashboardNavigationResponse.NavigationItem(
                        "Tenant Management", "/api/v1/admin/tenants", "business", "SUPER_ADMIN"),
                new DashboardNavigationResponse.NavigationItem(
                        "Global Settings", "/api/v1/admin/global-settings", "settings", "SUPER_ADMIN"),
                new DashboardNavigationResponse.NavigationItem(
                        "Platform Configuration", "/api/v1/admin/platform-configuration", "tune", "SUPER_ADMIN"),
                new DashboardNavigationResponse.NavigationItem(
                        "System Health", "/api/v1/admin/dashboard", "monitor_heart", "SUPER_ADMIN")
        );

        List<DashboardNavigationResponse.QuickAction> quickActions = List.of(
                new DashboardNavigationResponse.QuickAction(
                        "refresh-dashboard", "Refresh Dashboard", "/api/v1/admin/dashboard", "GET"),
                new DashboardNavigationResponse.QuickAction(
                        "create-tenant", "Create Tenant", "/api/v1/admin/tenants", "POST"),
                new DashboardNavigationResponse.QuickAction(
                        "view-alerts", "View System Alerts", "/api/v1/admin/dashboard", "GET")
        );

        return new DashboardNavigationResponse(items, quickActions);
    }
}
