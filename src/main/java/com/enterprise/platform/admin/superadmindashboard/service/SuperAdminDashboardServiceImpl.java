package com.enterprise.platform.admin.superadmindashboard.service;

import com.enterprise.platform.admin.superadmindashboard.client.PlatformHealthClient;
import com.enterprise.platform.admin.superadmindashboard.client.SubscriptionClient;
import com.enterprise.platform.admin.superadmindashboard.client.TenantClient;
import com.enterprise.platform.admin.superadmindashboard.client.UserClient;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminDashboardServiceImpl implements SuperAdminDashboardService {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminDashboardServiceImpl.class);

    private final PlatformHealthClient platformHealthClient;
    private final TenantClient tenantClient;
    private final UserClient userClient;
    private final SubscriptionClient subscriptionClient;

    public SuperAdminDashboardServiceImpl(
            PlatformHealthClient platformHealthClient,
            TenantClient tenantClient,
            UserClient userClient,
            SubscriptionClient subscriptionClient) {
        this.platformHealthClient = platformHealthClient;
        this.tenantClient = tenantClient;
        this.userClient = userClient;
        this.subscriptionClient = subscriptionClient;
    }

    @Override
    public SuperAdminDashboardResponse getDashboard() {
        List<String> systemAlerts = new ArrayList<>();
        List<String> recentActivities = new ArrayList<>();

        // 1. Platform Health Aggregation
        String healthStatus = platformHealthClient.getPlatformHealth();
        if ("UNKNOWN".equalsIgnoreCase(healthStatus) || "DOWN".equalsIgnoreCase(healthStatus)) {
            systemAlerts.add("Platform Health Warning: Core services are reporting " + healthStatus);
        }

        // 2. Storage Utilization Aggregation
        Double storageUtilization = platformHealthClient.getStorageUtilization().orElse(0.0);
        if (storageUtilization >= 80.0) {
            systemAlerts.add("Storage Warning: High disk utilization detected (" + storageUtilization + "%)");
        }

        // 3. Tenant Data Aggregation (Partial Failure Safe)
        Optional<Long> totalTenantsOpt = tenantClient.getTotalTenants();
        Long totalTenants = totalTenantsOpt.orElseGet(() -> {
            systemAlerts.add("Dependency Alert: Tenant Service is currently unavailable");
            return 0L;
        });
        recentActivities.addAll(tenantClient.getRecentTenantActivities());

        // 4. User & Session Data Aggregation (Partial Failure Safe)
        Optional<Long> totalUsersOpt = userClient.getTotalUsers();
        Long totalUsers = totalUsersOpt.orElseGet(() -> {
            systemAlerts.add("Dependency Alert: User Service is currently unavailable");
            return 0L;
        });
        Optional<Long> activeSessionsOpt = userClient.getActiveSessions();
        Long activeSessions = activeSessionsOpt.orElse(0L);

        // 5. Subscription & License Data Aggregation (Partial Failure Safe)
        Optional<Long> activeSubscriptionsOpt = subscriptionClient.getActiveSubscriptions();
        Long activeSubscriptions = activeSubscriptionsOpt.orElseGet(() -> {
            systemAlerts.add("Dependency Alert: Subscription/License Service is currently unavailable");
            return 0L;
        });
        systemAlerts.addAll(subscriptionClient.getSubscriptionAlerts());

        if (recentActivities.isEmpty()) {
            recentActivities.add("System operational. No critical recent administrative activities logged.");
        }

        return new SuperAdminDashboardResponse(
                totalTenants,
                totalUsers,
                activeSubscriptions,
                healthStatus,
                activeSessions,
                storageUtilization,
                systemAlerts,
                recentActivities
        );
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
}