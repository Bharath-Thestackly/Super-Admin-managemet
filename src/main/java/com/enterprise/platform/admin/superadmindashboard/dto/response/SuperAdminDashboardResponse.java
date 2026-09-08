
        package com.enterprise.platform.admin.superadmindashboard.dto.response;

import java.util.List;

public class SuperAdminDashboardResponse {

    private Long totalTenants;
    private Long totalUsers;
    private Long activeSubscriptions;
    private String platformHealthStatus;
    private Long activeSessions;
    private Double storageUtilization;
    private List<String> systemAlerts;
    private List<String> recentActivities;

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
        this.systemAlerts = systemAlerts;
        this.recentActivities = recentActivities;
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
}

