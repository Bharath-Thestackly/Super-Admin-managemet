package com.enterprise.superadmin.platformconfiguration.unit;

import com.enterprise.superadmin.platformconfiguration.controller.PlatformConfigurationController;
import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationCreateRequest;
import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationStatusUpdateRequest;
import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationUpdateRequest;
import com.enterprise.superadmin.platformconfiguration.dto.response.PlatformConfigurationHistoryResponse;
import com.enterprise.superadmin.platformconfiguration.dto.response.PlatformConfigurationResponse;
import com.enterprise.superadmin.platformconfiguration.exception.ConfigurationActivationException;
import com.enterprise.superadmin.platformconfiguration.exception.DuplicateConfigurationNameException;
import com.enterprise.superadmin.platformconfiguration.exception.GlobalExceptionHandler;
import com.enterprise.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException;
import com.enterprise.superadmin.platformconfiguration.service.PlatformConfigurationService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web boundary test suite for {@link PlatformConfigurationController}.
 *
 * <p><b>FRS Traceability:</b></p>
 * <ul>
 *   <li><b>Module:</b> Module 1 &mdash; Platform Administration</li>
 *   <li><b>Feature:</b> FR-001.2 Platform Configuration</li>
 *   <li><b>Scenarios:</b> REST endpoints, Search & Filter parameters, Bean Validation, HTTP 200, 201, 204, 400, 404, 409</li>
 * </ul>
 */
@WebMvcTest(controllers = PlatformConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PlatformConfigurationControllerTest {

    private static final Logger log = LoggerFactory.getLogger(PlatformConfigurationControllerTest.class);

    private static UUID sampleId;
    private static String validCreateJson;
    private static String validUpdateJson;
    private static String validStatusJson;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlatformConfigurationService service;

    private PlatformConfigurationResponse sampleResponse;

    @BeforeAll
    static void beforeAll() {
        log.info("Initializing static fixtures for PlatformConfigurationControllerTest");
        sampleId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        validCreateJson = """
                {
                  "configurationName": "Primary-Config",
                  "configurationCategory": "PLATFORM",
                  "description": "Primary Platform Configuration",
                  "status": "ACTIVE",
                  "platformName": "One Enterprise Cloud Platform",
                  "platformUrl": "https://platform.enterprise.com",
                  "environment": "DEVELOPMENT",
                  "defaultLanguage": "en",
                  "defaultTimeZone": "UTC",
                  "defaultCurrency": "USD",
                  "maintenanceMode": "DISABLED",
                  "featureToggle": "ENABLED",
                  "autoBackup": "ENABLED",
                  "sessionTimeout": 30,
                  "passwordExpiry": 90,
                  "maximumLoginAttempts": 5
                }
                """;

        validUpdateJson = """
                {
                  "configurationName": "Updated-Config",
                  "configurationCategory": "SYSTEM",
                  "description": "Updated Description",
                  "status": "INACTIVE",
                  "platformName": "Updated Platform",
                  "platformUrl": "https://updated.platform.com",
                  "environment": "STAGING",
                  "defaultLanguage": "fr",
                  "defaultTimeZone": "Europe/Paris",
                  "defaultCurrency": "EUR",
                  "maintenanceMode": "ENABLED",
                  "featureToggle": "DISABLED",
                  "autoBackup": "DISABLED",
                  "sessionTimeout": 60,
                  "passwordExpiry": 180,
                  "maximumLoginAttempts": 3
                }
                """;

        validStatusJson = """
                {
                  "status": "INACTIVE"
                }
                """;
    }

    @BeforeEach
    void setUp() {
        log.debug("Setting up mock response object for controller tests");
        sampleResponse = new PlatformConfigurationResponse(
                sampleId,
                "Primary-Config",
                "PLATFORM",
                "Primary Platform Configuration",
                "ACTIVE",
                LocalDate.now(),
                "One Enterprise Cloud Platform",
                "https://platform.enterprise.com",
                "DEVELOPMENT",
                "en",
                "UTC",
                "USD",
                "DISABLED",
                "ENABLED",
                "ENABLED",
                30,
                90,
                5,
                LocalDate.now(),
                "superadmin",
                LocalDate.now(),
                "superadmin"
        );
    }

    @AfterEach
    void tearDown() {
        log.debug("Resetting mock service and clearing instance references");
        sampleResponse = null;
        reset(service);
    }

    @AfterAll
    static void afterAll() {
        log.info("Tearing down static fixtures for PlatformConfigurationControllerTest");
        sampleId = null;
        validCreateJson = null;
        validUpdateJson = null;
        validStatusJson = null;
    }

    // =========================================================================
    // GET /api/v1/platform-configurations (UNFILTERED & FILTERED)
    // =========================================================================
    @Test
    @DisplayName("GET all - Returns 200 OK with list of configurations (unfiltered)")
    void getAll_shouldReturn200_whenConfigurationsExist() throws Exception {
        log.info("Executing test: getAll_shouldReturn200_whenConfigurationsExist");
        when(service.getAll(isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/platform-configurations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(sampleId.toString()))
                .andExpect(jsonPath("$[0].configurationName").value("Primary-Config"));
    }

    @Test
    @DisplayName("GET all - Returns 200 OK with empty array when no configurations exist")
    void getAll_shouldReturn200EmptyArray_whenNoneExist() throws Exception {
        log.info("Executing test: getAll_shouldReturn200EmptyArray_whenNoneExist");
        when(service.getAll(isNull(), isNull(), isNull(), isNull())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/platform-configurations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET all with filter params - Passes parameters to service and returns 200 OK")
    void getAll_withFilterParameters_shouldPassParamsToServiceAndReturn200() throws Exception {
        log.info("Executing test: getAll_withFilterParameters_shouldPassParamsToServiceAndReturn200");
        when(service.getAll(eq("Primary"), eq("PLATFORM"), eq("DEVELOPMENT"), eq("ACTIVE")))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/platform-configurations")
                        .param("name", "Primary")
                        .param("category", "PLATFORM")
                        .param("environment", "DEVELOPMENT")
                        .param("status", "ACTIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].configurationName").value("Primary-Config"));
    }

    // =========================================================================
    // GET /api/v1/platform-configurations/{id}
    // =========================================================================
    @Test
    @DisplayName("GET by ID - Returns 200 OK with configuration when found")
    void getById_shouldReturn200_whenFound() throws Exception {
        log.info("Executing test: getById_shouldReturn200_whenFound");
        when(service.getById(sampleId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/platform-configurations/{id}", sampleId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleId.toString()))
                .andExpect(jsonPath("$.configurationName").value("Primary-Config"));
    }

    @Test
    @DisplayName("GET by ID - Returns 404 NOT FOUND when ID missing or deleted")
    void getById_shouldReturn404_whenNotFound() throws Exception {
        log.info("Executing test: getById_shouldReturn404_whenNotFound");
        when(service.getById(sampleId)).thenThrow(new PlatformConfigurationNotFoundException("Config not found"));

        mockMvc.perform(get("/api/v1/platform-configurations/{id}", sampleId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Config not found"));
    }

    // =========================================================================
    // POST /api/v1/platform-configurations
    // =========================================================================
    @Test
    @DisplayName("POST create - Returns 201 CREATED when request is valid")
    void create_shouldReturn201_whenValid() throws Exception {
        log.info("Executing test: create_shouldReturn201_whenValid");
        when(service.create(any(PlatformConfigurationCreateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/platform-configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sampleId.toString()));
    }

    @Test
    @DisplayName("POST create - Returns 400 BAD REQUEST when validation fails")
    void create_shouldReturn400_whenPayloadInvalid() throws Exception {
        log.info("Executing test: create_shouldReturn400_whenPayloadInvalid");
        String invalidJson = """
                {
                  "configurationName": "",
                  "platformUrl": "not-a-url",
                  "sessionTimeout": -10
                }
                """;

        mockMvc.perform(post("/api/v1/platform-configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("POST create - Returns 409 CONFLICT on duplicate configuration name")
    void create_shouldReturn409_whenNameDuplicated() throws Exception {
        log.info("Executing test: create_shouldReturn409_whenNameDuplicated");
        when(service.create(any(PlatformConfigurationCreateRequest.class)))
                .thenThrow(new DuplicateConfigurationNameException("Duplicate name"));

        mockMvc.perform(post("/api/v1/platform-configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Duplicate name"));
    }

    // =========================================================================
    // PUT /api/v1/platform-configurations/{id}
    // =========================================================================
    @Test
    @DisplayName("PUT update - Returns 200 OK when update is valid")
    void update_shouldReturn200_whenValid() throws Exception {
        log.info("Executing test: update_shouldReturn200_whenValid");
        when(service.update(eq(sampleId), any(PlatformConfigurationUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/platform-configurations/{id}", sampleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleId.toString()));
    }

    @Test
    @DisplayName("PUT update - Returns 404 NOT FOUND when configuration missing")
    void update_shouldReturn404_whenNotFound() throws Exception {
        log.info("Executing test: update_shouldReturn404_whenNotFound");
        when(service.update(eq(sampleId), any(PlatformConfigurationUpdateRequest.class)))
                .thenThrow(new PlatformConfigurationNotFoundException("Config not found"));

        mockMvc.perform(put("/api/v1/platform-configurations/{id}", sampleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // =========================================================================
    // PATCH /api/v1/platform-configurations/{id}/status
    // =========================================================================
    @Test
    @DisplayName("PATCH status - Returns 200 OK on successful status change")
    void updateStatus_shouldReturn200_whenValid() throws Exception {
        log.info("Executing test: updateStatus_shouldReturn200_whenValid");
        when(service.updateStatus(eq(sampleId), any(PlatformConfigurationStatusUpdateRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/v1/platform-configurations/{id}/status", sampleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStatusJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PATCH status - Returns 400 BAD REQUEST when pre-activation validation fails")
    void updateStatus_shouldReturn400_whenActivationFails() throws Exception {
        log.info("Executing test: updateStatus_shouldReturn400_whenActivationFails");
        when(service.updateStatus(eq(sampleId), any(PlatformConfigurationStatusUpdateRequest.class)))
                .thenThrow(new ConfigurationActivationException("Auto-Backup DISABLED is not permitted for PRODUCTION environment"));

        mockMvc.perform(patch("/api/v1/platform-configurations/{id}/status", sampleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACTIVE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("ERR-0009"))
                .andExpect(jsonPath("$.message").value("Auto-Backup DISABLED is not permitted for PRODUCTION environment"));
    }

    // =========================================================================
    // DELETE /api/v1/platform-configurations/{id}
    // =========================================================================
    @Test
    @DisplayName("DELETE - Returns 204 NO CONTENT when soft delete succeeds")
    void delete_shouldReturn204_whenDeleted() throws Exception {
        log.info("Executing test: delete_shouldReturn204_whenDeleted");
        doNothing().when(service).delete(sampleId);

        mockMvc.perform(delete("/api/v1/platform-configurations/{id}", sampleId))
                .andExpect(status().isNoContent());
    }

    // =========================================================================
    // POST /api/v1/platform-configurations/{id}/restore-default
    // =========================================================================
    @Test
    @DisplayName("POST restore-default - Returns 200 OK with restored defaults")
    void restoreDefault_shouldReturn200_whenSuccessful() throws Exception {
        log.info("Executing test: restoreDefault_shouldReturn200_whenSuccessful");
        when(service.restoreDefaultConfiguration(sampleId)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/platform-configurations/{id}/restore-default", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformName").value("One Enterprise Cloud Platform"));
    }

    // =========================================================================
    // GET /api/v1/platform-configurations/{id}/history
    // =========================================================================
    @Test
    @DisplayName("GET history - Returns 200 OK with list of history snapshots")
    void getHistory_shouldReturn200_whenHistoryExists() throws Exception {
        log.info("Executing test: getHistory_shouldReturn200_whenHistoryExists");
        PlatformConfigurationHistoryResponse historyItem = new PlatformConfigurationHistoryResponse(
                UUID.randomUUID(),
                sampleId,
                1,
                "Primary-Config",
                "PLATFORM",
                "Primary Platform Configuration",
                "ACTIVE",
                LocalDate.now(),
                "One Enterprise Cloud Platform",
                "https://platform.enterprise.com",
                "DEVELOPMENT",
                "en",
                "UTC",
                "USD",
                "DISABLED",
                "ENABLED",
                "ENABLED",
                30,
                90,
                5,
                LocalDate.now(),
                "superadmin",
                "INITIAL_CREATION"
        );
        when(service.getHistory(sampleId)).thenReturn(List.of(historyItem));

        mockMvc.perform(get("/api/v1/platform-configurations/{id}/history", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[0].changeReason").value("INITIAL_CREATION"));
    }

    // =========================================================================
    // POST /api/v1/platform-configurations/{id}/rollback/{version}
    // =========================================================================
    @Test
    @DisplayName("POST rollback - Returns 200 OK with rolled back configuration")
    void rollback_shouldReturn200_whenSuccessful() throws Exception {
        log.info("Executing test: rollback_shouldReturn200_whenSuccessful");
        when(service.rollbackToVersion(eq(sampleId), eq(1))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/platform-configurations/{id}/rollback/{version}", sampleId, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurationName").value("Primary-Config"));
    }

    @Test
    @DisplayName("GET history - Returns 404 NOT FOUND when configuration does not exist")
    void getHistory_shouldReturn404_whenConfigNotFound() throws Exception {
        log.info("Executing test: getHistory_shouldReturn404_whenConfigNotFound");
        when(service.getHistory(sampleId))
                .thenThrow(new PlatformConfigurationNotFoundException("Configuration not found"));

        mockMvc.perform(get("/api/v1/platform-configurations/{id}/history", sampleId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST rollback - Returns 404 NOT FOUND when version snapshot not found")
    void rollback_shouldReturn404_whenVersionNotFound() throws Exception {
        log.info("Executing test: rollback_shouldReturn404_whenVersionNotFound");
        when(service.rollbackToVersion(eq(sampleId), eq(99)))
                .thenThrow(new PlatformConfigurationNotFoundException("Version snapshot not found"));

        mockMvc.perform(post("/api/v1/platform-configurations/{id}/rollback/{version}", sampleId, 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST create - Accepts request payload when createdBy and updatedBy are omitted")
    void create_shouldAcceptPayload_whenAuditFieldsOmitted() throws Exception {
        log.info("Executing test: create_shouldAcceptPayload_whenAuditFieldsOmitted");
        when(service.create(any(PlatformConfigurationCreateRequest.class))).thenReturn(sampleResponse);

        String jsonWithoutAuditFields = """
                {
                  "configurationName": "Primary-Config",
                  "configurationCategory": "PLATFORM",
                  "description": "Primary Platform Configuration",
                  "status": "ACTIVE",
                  "platformName": "One Enterprise Cloud Platform",
                  "platformUrl": "https://platform.enterprise.com",
                  "environment": "DEVELOPMENT",
                  "defaultLanguage": "en",
                  "defaultTimeZone": "UTC",
                  "defaultCurrency": "USD",
                  "maintenanceMode": "DISABLED",
                  "featureToggle": "ENABLED",
                  "autoBackup": "ENABLED",
                  "sessionTimeout": 30,
                  "passwordExpiry": 90,
                  "maximumLoginAttempts": 5
                }
                """;

        mockMvc.perform(post("/api/v1/platform-configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutAuditFields))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.configurationName").value("Primary-Config"));

        verify(service, times(1)).create(any(PlatformConfigurationCreateRequest.class));
    }

    @Test
    @DisplayName("PUT update - Accepts request payload when updatedBy is omitted")
    void update_shouldAcceptPayload_whenUpdatedByOmitted() throws Exception {
        log.info("Executing test: update_shouldAcceptPayload_whenUpdatedByOmitted");
        when(service.update(eq(sampleId), any(PlatformConfigurationUpdateRequest.class))).thenReturn(sampleResponse);

        String jsonWithoutUpdatedBy = """
                {
                  "configurationName": "Updated-Config",
                  "configurationCategory": "PLATFORM",
                  "description": "Updated Description",
                  "status": "ACTIVE",
                  "platformName": "Updated Platform",
                  "platformUrl": "https://platform.enterprise.com",
                  "environment": "PRODUCTION",
                  "defaultLanguage": "en",
                  "defaultTimeZone": "UTC",
                  "defaultCurrency": "USD",
                  "maintenanceMode": "DISABLED",
                  "featureToggle": "ENABLED",
                  "autoBackup": "ENABLED",
                  "sessionTimeout": 60,
                  "passwordExpiry": 60,
                  "maximumLoginAttempts": 3
                }
                """;

        mockMvc.perform(put("/api/v1/platform-configurations/{id}", sampleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutUpdatedBy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurationName").value("Primary-Config"));

        verify(service, times(1)).update(eq(sampleId), any(PlatformConfigurationUpdateRequest.class));
    }

    @Test
    @DisplayName("PATCH status - Accepts request payload when updatedBy is omitted")
    void updateStatus_shouldAcceptPayload_whenUpdatedByOmitted() throws Exception {
        log.info("Executing test: updateStatus_shouldAcceptPayload_whenUpdatedByOmitted");
        when(service.updateStatus(eq(sampleId), any(PlatformConfigurationStatusUpdateRequest.class))).thenReturn(sampleResponse);

        String jsonWithoutUpdatedBy = """
                {
                  "status": "INACTIVE"
                }
                """;

        mockMvc.perform(patch("/api/v1/platform-configurations/{id}/status", sampleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutUpdatedBy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurationName").value("Primary-Config"));

        verify(service, times(1)).updateStatus(eq(sampleId), any(PlatformConfigurationStatusUpdateRequest.class));
    }

    // =========================================================================
    // RESTORE DEFAULTS, HISTORY & ROLLBACK ENDPOINTS
    // =========================================================================

    @Test
    @DisplayName("POST restore-default - Returns 200 OK with restored defaults")
    void restoreDefaultConfiguration_shouldReturn200() throws Exception {
        log.info("Executing test: restoreDefaultConfiguration_shouldReturn200");
        when(service.restoreDefaultConfiguration(sampleId)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/platform-configurations/{id}/restore-default", sampleId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurationName").value("Primary-Config"));

        verify(service, times(1)).restoreDefaultConfiguration(sampleId);
    }

    @Test
    @DisplayName("GET history - Returns 200 OK with history snapshots list")
    void getHistory_shouldReturn200() throws Exception {
        log.info("Executing test: getHistory_shouldReturn200");
        PlatformConfigurationHistoryResponse historySnapshot = new PlatformConfigurationHistoryResponse(
                UUID.randomUUID(), sampleId, 1, "Primary-Config", "PLATFORM", "Description",
                "ACTIVE", LocalDate.now(), "Platform", "https://platform.com", "DEVELOPMENT",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                30, 90, 5, LocalDate.now(), "admin", "Initial snapshot"
        );
        when(service.getHistory(sampleId)).thenReturn(List.of(historySnapshot));

        mockMvc.perform(get("/api/v1/platform-configurations/{id}/history", sampleId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].configurationName").value("Primary-Config"))
                .andExpect(jsonPath("$[0].changeReason").value("Initial snapshot"));

        verify(service, times(1)).getHistory(sampleId);
    }

    @Test
    @DisplayName("POST rollback - Returns 200 OK with restored configuration")
    void rollback_shouldReturn200() throws Exception {
        log.info("Executing test: rollback_shouldReturn200");
        when(service.rollbackToVersion(sampleId, 1)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/platform-configurations/{id}/rollback/{version}", sampleId, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurationName").value("Primary-Config"));

        verify(service, times(1)).rollbackToVersion(sampleId, 1);
    }
}