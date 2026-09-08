package com.enterprise.platform.admin.superadmindashboard.service;

import com.enterprise.platform.admin.superadmindashboard.client.PlatformHealthClient;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminDashboardServiceImplTest {

    @Mock
    private PlatformHealthClient platformHealthClient;

    @Test
    void getDashboardSummary_whenHealthServiceFails_returnsUnknownInsteadOfCrashing() {
        when(platformHealthClient.getPlatformHealth())
                .thenThrow(new ResourceAccessException("Connection refused"));

        SuperAdminDashboardServiceImpl service =
                new SuperAdminDashboardServiceImpl(platformHealthClient, new ObjectMapper());

        SuperAdminDashboardResponse response = service.getDashboardSummary();

        assertThat(response).isNotNull();
        assertThat(response.getPlatformHealthStatus()).isEqualTo("UNKNOWN");
    }

    @Test
    void getDashboardSummary_whenHealthServiceReturnsValidJson_parsesStatusCorrectly() {
        when(platformHealthClient.getPlatformHealth())
                .thenReturn("{\"status\":\"UP\"}");

        SuperAdminDashboardServiceImpl service =
                new SuperAdminDashboardServiceImpl(platformHealthClient, new ObjectMapper());

        SuperAdminDashboardResponse response = service.getDashboardSummary();

        assertThat(response.getPlatformHealthStatus()).isEqualTo("UP");
    }
}