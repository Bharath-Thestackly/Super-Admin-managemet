package com.enterprise.superadmin.globaldashboard.integration.client;

import com.enterprise.superadmin.globaldashboard.integration.dto.TenantSummaryResponse;

public interface TenantManagementClient {

    TenantSummaryResponse getTenantSummary();
}