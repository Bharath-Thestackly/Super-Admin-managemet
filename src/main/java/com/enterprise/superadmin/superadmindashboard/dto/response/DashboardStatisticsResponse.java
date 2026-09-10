package com.enterprise.superadmin.superadmindashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dashboard statistics required by the approved FRS")
public class DashboardStatisticsResponse {

    @Schema(description = "Total tenants count", example = "35")
    private Long totalTenants;

    @Schema(description = "Total users count", example = "580")
    private Long totalUsers;

    @Schema(description = "Active subscriptions count", example = "32")
    private Long activeSubscriptions;

    @Schema(description = "Platform health status", example = "UP")
    private String platformHealthStatus;

    @Schema(description = "Active user sessions count", example = "142")
    private Long activeSessions;

    @Schema(description = "Platform storage utilization percentage", example = "68.5")
    private Double storageUtilization;

    @Schema(description = "Total number of active system alerts", example = "2")
    private Long systemAlertsCount;

    public DashboardStatisticsResponse() {
    }

    public DashboardStatisticsResponse(Long totalTenants, Long totalUsers, Long activeSubscriptions,
                                       String platformHealthStatus, Long activeSessions,
                                       Double storageUtilization, Long systemAlertsCount) {
        this.totalTenants = totalTenants;
        this.totalUsers = totalUsers;
        this.activeSubscriptions = activeSubscriptions;
        this.platformHealthStatus = platformHealthStatus;
        this.activeSessions = activeSessions;
        this.storageUtilization = storageUtilization;
        this.systemAlertsCount = systemAlertsCount;
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

    public Long getSystemAlertsCount() {
        return systemAlertsCount;
    }

    public void setSystemAlertsCount(Long systemAlertsCount) {
        this.systemAlertsCount = systemAlertsCount;
    }
}