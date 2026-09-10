package com.enterprise.platform.admin.superadmindashboard.controller;

import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardNavigationResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardStatisticsResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.DashboardSummaryResponse;
import com.enterprise.platform.admin.superadmindashboard.dto.response.SuperAdminDashboardResponse;
import com.enterprise.platform.admin.superadmindashboard.service.SuperAdminDashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SuperAdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuperAdminDashboardService dashboardService;

    @Test
    @DisplayName("GET /api/v1/admin/dashboard returns 401 when unauthenticated")
    void testGetDashboardUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard returns 403 when user is not SUPER_ADMIN")
    @WithMockUser(roles = "USER")
    void testGetDashboardForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard returns 200 with consolidated data for SUPER_ADMIN")
    @WithMockUser(roles = "SUPER_ADMIN")
    void testGetDashboardSuccess() throws Exception {
        SuperAdminDashboardResponse response = new SuperAdminDashboardResponse(
                45L, 620L, 40L, "UP", 150L, 72.4,
                List.of("System Alert 1"),
                List.of("New Tenant ABC registered")
        );

        when(dashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTenants").value(45))
                .andExpect(jsonPath("$.totalUsers").value(620))
                .andExpect(jsonPath("$.activeSubscriptions").value(40))
                .andExpect(jsonPath("$.platformHealthStatus").value("UP"))
                .andExpect(jsonPath("$.activeSessions").value(150))
                .andExpect(jsonPath("$.storageUtilization").value(72.4))
                .andExpect(jsonPath("$.systemAlerts[0]").value("System Alert 1"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard/summary returns 200 with summary data")
    @WithMockUser(roles = "SUPER_ADMIN")
    void testGetDashboardSummary() throws Exception {
        DashboardSummaryResponse summary = new DashboardSummaryResponse(45L, 620L, 40L, "UP");

        when(dashboardService.getDashboardSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/admin/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTenants").value(45))
                .andExpect(jsonPath("$.totalUsers").value(620))
                .andExpect(jsonPath("$.activeSubscriptions").value(40))
                .andExpect(jsonPath("$.platformHealthStatus").value("UP"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard/statistics returns 200 with statistics data")
    @WithMockUser(roles = "SUPER_ADMIN")
    void testGetDashboardStatistics() throws Exception {
        DashboardStatisticsResponse stats = new DashboardStatisticsResponse(
                45L, 620L, 40L, "UP", 150L, 72.4, 1L
        );

        when(dashboardService.getDashboardStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/dashboard/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTenants").value(45))
                .andExpect(jsonPath("$.systemAlertsCount").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard/navigation returns 200 with navigation data for SUPER_ADMIN")
    @WithMockUser(roles = "SUPER_ADMIN")
    void testGetDashboardNavigationSuccess() throws Exception {
        DashboardNavigationResponse navigation = new DashboardNavigationResponse(
                List.of(new DashboardNavigationResponse.NavigationItem(
                        "Tenant Management", "/api/v1/admin/tenants", "business", "SUPER_ADMIN")),
                List.of()
        );

        when(dashboardService.getDashboardNavigation()).thenReturn(navigation);

        mockMvc.perform(get("/api/v1/admin/dashboard/navigation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Tenant Management"))
                .andExpect(jsonPath("$.items[0].path").value("/api/v1/admin/tenants"));
    }
}
