package com.enterprise.superadmin.superadmindashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Consolidated Super Admin Dashboard Response")
public class SuperAdminDashboardResponse {

    @Schema(description = "Total number of registered tenants in the platform", example = "35")
    private Long totalTenants;

    @Schema(description = "Total number of users registered across all tenants", example = "580")
    private Long totalUsers;

    @Schema(description = "Total number of currently active subscriptions", example = "32")
    private Long activeSubscriptions;

    @Schema(description = "Consolidated platform operational health status", example = "UP")
    private String platformHealthStatus;

    @Schema(description = "Total number of real-time active user sessions", example = "142")
    private Long activeSessions;

    @Schema(description = "Current platform storage utilization percentage", example = "68.5")
    private Double storageUtilization;

    @Schema(description = "List of current platform and dependency alerts")
    private List<String> systemAlerts = new ArrayList<>();

    @Schema(description = "Recent administrative activities and tenant events")
    private List<String> recentActivities = new ArrayList<>();

    @Schema(description = "Dashboard data retrieval timestamp")
    private Instant timestamp;

    public SuperAdminDashboardResponse() {
    }

    public SuperAdminDashboardResponse(
            Long totalTenants,
            Long totalUsers,
            Long activeSubscriptions,
            String platformHealthStatus,
            Long activeSessions,
            Double storageUtilization,
            List<String> systemAlerts,
            List<String> recentActivities) {
        this.totalTenants = totalTenants;
        this.totalUsers = totalUsers;
        this.activeSubscriptions = activeSubscriptions;
        this.platformHealthStatus = platformHealthStatus;
        this.activeSessions = activeSessions;
        this.storageUtilization = storageUtilization;
        this.systemAlerts = systemAlerts != null ? systemAlerts : new ArrayList<>();
        this.recentActivities = recentActivities != null ? recentActivities : new ArrayList<>();
    }

    public Long getTotalTenants() {
        return totalTenants;
    }

    public void setTotalTenants(Long totalTenants) {
        this.totalTenants = totalTenants;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(Long activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public String getPlatformHealthStatus() {
        return platformHealthStatus;
    }

    public void setPlatformHealthStatus(String platformHealthStatus) {
        this.platformHealthStatus = platformHealthStatus;
    }

    public Long getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(Long activeSessions) {
        this.activeSessions = activeSessions;
    }

    public Double getStorageUtilization() {
        return storageUtilization;
    }

    public void setStorageUtilization(Double storageUtilization) {
        this.storageUtilization = storageUtilization;
    }

    public List<String> getSystemAlerts() {
        return systemAlerts;
    }

    public void setSystemAlerts(List<String> systemAlerts) {
        this.systemAlerts = systemAlerts;
    }

    public List<String> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<String> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
