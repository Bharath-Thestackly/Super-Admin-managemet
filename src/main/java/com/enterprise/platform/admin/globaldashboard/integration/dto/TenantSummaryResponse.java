package com.enterprise.platform.admin.globaldashboard.integration.dto;

public record TenantSummaryResponse(
        long totalTenants,
        long activeTenants
) {
}