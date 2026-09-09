package com.enterprise.superadmin.controller;


import com.enterprise.superadmin.license_management_service.controller.LicenseController;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseStatusResponse;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.service.LicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LicenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class LicenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LicenseService licenseService;

    @Test
    void shouldGetLicenses() throws Exception {

        LicenseResponse response =
                new LicenseResponse(
                        UUID.randomUUID(),
                        "LIC-123456789",
                        "PREMIUM",
                        LicenseType.SUBSCRIPTION,
                        LocalDate.now(),
                        LocalDate.now().plusDays(365),
                        LicenseStatus.ACTIVE,
                        null,
                        null
                );

        when(
                licenseService.getLicenses(
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/licenses")
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$[0].licenseKey")
                                .value("LIC-123456789")
                )
                .andExpect(
                        jsonPath("$[0].status")
                                .value("ACTIVE")
                );
    }

    @Test
    void shouldGetLicenseStatus() throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        LicenseStatusResponse response =
                new LicenseStatusResponse(
                        licenseId,
                        "LIC-123456789",
                        LicenseStatus.ACTIVE,
                        LocalDate.now(),
                        LocalDate.now().plusDays(30),
                        30
                );

        when(
                licenseService.getLicenseStatus(
                        licenseId
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/licenses/{id}/status",
                                licenseId
                        )
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.licenseKey")
                                .value("LIC-123456789")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                );
    }

    @Test
    void shouldGetExpiringLicenses() throws Exception {

        when(
                licenseService.getExpiringLicenses(30)
        ).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/licenses/expiring")
                                .param("days", "30")
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().json("[]")
                );
    }
}