package com.enterprise.superadmin.globaldashboard;

import com.enterprise.superadmin.globaldashboard.controller.GlobalDashboardController;
import com.enterprise.superadmin.globaldashboard.service.GlobalDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalDashboardController.class)
@WithMockUser
class GlobalDashboardControllerTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;
    @MockitoBean
    private GlobalDashboardService globalDashboardService;



    @Test
    void globalDashboard_shouldReturn200() throws Exception {

        when(globalDashboardService.getDashboard())
                .thenReturn(null);

        mockMvc.perform(
                get("/api/v1/global-dashboard")
        ).andExpect(status().isOk());

        verify(globalDashboardService).getDashboard();
    }


    @Test
    void summary_shouldReturn200() throws Exception {

        when(globalDashboardService.getSummary())
                .thenReturn(null);

        mockMvc.perform(
                get("/api/v1/global-dashboard/summary")
        ).andExpect(status().isOk());

        verify(globalDashboardService).getSummary();
    }


    @Test
    void metrics_shouldReturn200() throws Exception {

        when(globalDashboardService.getMetrics())
                .thenReturn(null);

        mockMvc.perform(
                get("/api/v1/global-dashboard/metrics")
        ).andExpect(status().isOk());

        verify(globalDashboardService).getMetrics();
    }


    @Test
    void status_shouldReturn200() throws Exception {

        when(globalDashboardService.getStatus())
                .thenReturn(null);

        mockMvc.perform(
                get("/api/v1/global-dashboard/status")
        ).andExpect(status().isOk());

        verify(globalDashboardService).getStatus();
    }
}