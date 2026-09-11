package com.enterprise.superadmin.platform_settings_service.controller;

import com.enterprise.superadmin.platform_settings_service.dto.response.PlatformSettingsHistoryResponse;
import com.enterprise.superadmin.platform_settings_service.dto.response.PlatformSettingsResponse;
import com.enterprise.superadmin.platform_settings_service.service.PlatformSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlatformSettingsController.class)
public class PlatformSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlatformSettingsService service;

    @Test
    void shouldGetAllSettings() throws Exception {

        PlatformSettingsResponse response = PlatformSettingsResponse.builder()
                .id(UUID.randomUUID())
                .settingName("GLOBAL_SETTINGS")
                .build();

        when(service.getAllSettings(null, null, null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/platform-settings")
                        .with(user("testuser").roles("SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].settingName")
                        .value("GLOBAL_SETTINGS"));
    }

    @Test
    void shouldSearchAndFilterSettings() throws Exception {

        PlatformSettingsResponse response =
                PlatformSettingsResponse.builder()
                        .id(UUID.randomUUID())
                        .settingName("GLOBAL_SETTINGS")
                        .build();

        when(service.getAllSettings("global", "PLATFORM", null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/platform-settings")
                        .param("search", "global")
                        .param("category", "PLATFORM")
                        .with(user("testuser").roles("SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].settingName").value("GLOBAL_SETTINGS"));
    }

    @Test
    void shouldGetSettingHistory() throws Exception {

        PlatformSettingsHistoryResponse response =
                PlatformSettingsHistoryResponse.builder()
                        .historyId(UUID.randomUUID())
                        .settingId(UUID.randomUUID())
                        .versionNumber(2L)
                        .build();

        when(service.getSettingHistory("GLOBAL_SETTINGS"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/platform-settings/GLOBAL_SETTINGS/history")
                        .with(user("testuser").roles("SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].versionNumber").value(2));
    }

    @Test
    void shouldExportSettingsAsCsv() throws Exception {

        byte[] csv = "Setting ID,Setting Name\n1,GLOBAL_SETTINGS\n"
                .getBytes(StandardCharsets.UTF_8);

        when(service.exportSettings(null, null, null)).thenReturn(csv);

        mockMvc.perform(get("/api/v1/platform-settings/export")
                        .with(user("testuser").roles("SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=platform-settings.csv"))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().bytes(csv));
    }

}