package com.enterprise.platform.admin.globaldashboard.integration.client;

import com.enterprise.platform.admin.globaldashboard.integration.dto.PlatformHealthResponse;

public interface PlatformHealthClient {

    PlatformHealthResponse getPlatformHealth();
}