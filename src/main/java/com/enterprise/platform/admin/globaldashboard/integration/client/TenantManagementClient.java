package com.enterprise.platform.admin.globaldashboard.integration.client;

import com.enterprise.platform.admin.globaldashboard.integration.dto.TenantSummaryResponse;

public interface TenantManagementClient {

    TenantSummaryResponse getTenantSummary();
}