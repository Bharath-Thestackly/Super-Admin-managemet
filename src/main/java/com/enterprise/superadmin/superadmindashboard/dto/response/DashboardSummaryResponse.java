package com.enterprise.superadmin.superadmindashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "High-level platform summary counts required by the UI")
public class DashboardSummaryResponse {

    @Schema(description = "Total registered tenants", example = "35")
    private Long totalTenants;

    @Schema(description = "Total registered users", example = "580")
    private Long totalUsers;

    @Schema(description = "Active subscriptions count", example = "32")
    private Long activeSubscriptions;

    @Schema(description = "Current platform health status", example = "UP")
    private String platformHealthStatus;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(Long totalTenants, Long totalUsers, Long activeSubscriptions, String platformHealthStatus) {
        this.totalTenants = totalTenants;
        this.totalUsers = totalUsers;
        this.activeSubscriptions = activeSubscriptions;
        this.platformHealthStatus = platformHealthStatus;
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
}