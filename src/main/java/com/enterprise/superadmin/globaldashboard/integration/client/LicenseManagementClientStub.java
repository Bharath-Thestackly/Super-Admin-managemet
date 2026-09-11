package com.enterprise.superadmin.globaldashboard.integration.client;

import com.enterprise.superadmin.globaldashboard.integration.dto.LicenseSummaryResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LicenseManagementClientStub
        implements LicenseManagementClient {

    @Override
    public LicenseSummaryResponse getLicenseSummary() {
        return new LicenseSummaryResponse(142);
    }
}