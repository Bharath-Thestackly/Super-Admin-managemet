package com.enterprise.platform.admin.superadmindashboard.controller;

import com.enterprise.platform.admin.superadmindashboard.config.RoleHeaderAuthenticationFilter;
import com.enterprise.platform.admin.superadmindashboard.config.SecurityConfig;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.enterprise.platform.admin.superadmindashboard.service.SuperAdminDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuperAdminDashboardController.class)
@Import({SecurityConfig.class, RoleHeaderAuthenticationFilter.class})
class SuperAdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SuperAdminDashboardService dashboardService;

    @Test
    void getSummary_withoutSuperAdminRole_isRejected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getSummary_withSuperAdminRole_returnsOk() throws Exception {
        SuperAdminDashboardResponse response = new SuperAdminDashboardResponse(
                25L, 450L, 18L, "UP", 127L, 68.5,
                List.of("License renewal due for Tenant ABC"),
                List.of("Tenant ABC created"));

        when(dashboardService.getDashboardSummary()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTenants").value(25))
                .andExpect(jsonPath("$.platformHealthStatus").value("UP"));
    }

    @Test
    void getDashboard_withoutSuperAdminRole_isRejected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getStatistics_withSuperAdminRole_returnsOk() throws Exception {
        SuperAdminDashboardResponse response = new SuperAdminDashboardResponse(
                25L, 450L, 18L, "UP", 127L, 68.5,
                List.of(), List.of());

        when(dashboardService.getDashboardStatistics()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard/statistics")
                        .header("X-User-Role", "SUPER_ADMIN"))
                .andExpect(status().isOk());
    }
}