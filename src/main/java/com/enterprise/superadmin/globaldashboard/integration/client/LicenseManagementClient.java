package com.enterprise.superadmin.globaldashboard.integration.client;

import com.enterprise.superadmin.globaldashboard.integration.dto.LicenseSummaryResponse;

public interface LicenseManagementClient {

    LicenseSummaryResponse getLicenseSummary();
}