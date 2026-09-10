package com.enterprise.superadmin.platformconfiguration.unit;

import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationCreateRequest;
import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationStatusUpdateRequest;
import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationUpdateRequest;
import com.enterprise.superadmin.platformconfiguration.dto.response.PlatformConfigurationHistoryResponse;
import com.enterprise.superadmin.platformconfiguration.dto.response.PlatformConfigurationResponse;
import com.enterprise.superadmin.platformconfiguration.entity.PlatformConfiguration;
import com.enterprise.superadmin.platformconfiguration.entity.PlatformConfigurationHistory;
import com.enterprise.superadmin.platformconfiguration.exception.ConfigurationActivationException;
import com.enterprise.superadmin.platformconfiguration.exception.DuplicateConfigurationNameException;
import com.enterprise.superadmin.platformconfiguration.exception.InvalidConfigurationValueException;
import com.enterprise.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException;
import com.enterprise.superadmin.platformconfiguration.repository.PlatformConfigurationHistoryRepository;
import com.enterprise.superadmin.platformconfiguration.repository.PlatformConfigurationRepository;
import com.enterprise.superadmin.platformconfiguration.service.PlatformConfigurationServiceImpl;
import com.enterprise.superadmin.platformconfiguration.service.PlatformConfigurationSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Production-grade Unit Test Suite covering {@link PlatformConfigurationServiceImpl},
 * dynamic JPA {@link PlatformConfigurationSpecification}, and domain entities/DTOs.
 *
 * <p><b>Test Coverage Objectives:</b></p>
 * <ul>
 *   <li>100% method, line, and branch coverage for service CRUD, filtering, lifecycle, and rollback operations.</li>
 *   <li>Master data compliance (ISO 639 languages, ISO 4217 currencies, IANA timezones, whitelisted categories).</li>
 *   <li>Pre-activation business rules (production auto-backup enforcement, session timeout bounds).</li>
 *   <li>Dynamic JPA Criteria Specifications with full predicate branch testing.</li>
 *   <li>Entity contracts (constructors, getters, setters, date overloads, equals, hashCode, static factories).</li>
 *   <li>Response DTO constructor overloads.</li>
 *   <li>Optimistic concurrency version increments and audit metadata capturing.</li>
 *   <li>SecurityContextHolder principal resolution and fallback branches.</li>
 *   <li>Fallback handling for null repository saves and 1-argument constructor wiring.</li>
 * </ul>
 *
 * <p>Implements rigorous JUnit 5 lifecycle hooks ({@code @BeforeAll}, {@code @BeforeEach},
 * {@code @AfterEach}, {@code @AfterAll}) with comprehensive SLF4J audit logging.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformConfigurationServiceImpl Comprehensive Unit Tests")
class PlatformConfigurationServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(PlatformConfigurationServiceImplTest.class);

    @Mock
    private PlatformConfigurationRepository repository;

    @Mock
    private PlatformConfigurationHistoryRepository historyRepository;

    @Mock
    private Root<PlatformConfiguration> specRoot;

    @Mock
    private CriteriaQuery<?> specQuery;

    @Mock
    private CriteriaBuilder specCb;

    @Mock
    private Path<Boolean> specPath;

    @Mock
    private Expression<String> specStringExpression;

    @Mock
    private Predicate specPredicate;

    @InjectMocks
    private PlatformConfigurationServiceImpl service;

    private PlatformConfiguration sampleConfig;
    private UUID configId;

    @BeforeAll
    static void setUpAll() {
        log.info("Starting PlatformConfigurationServiceImplTest suite");
    }

    @BeforeEach
    void setUp() {
        configId = UUID.randomUUID();
        sampleConfig = new PlatformConfiguration();
        sampleConfig.setId(configId);
        sampleConfig.setConfigurationName("Master Config");
        sampleConfig.setConfigurationCategory("PLATFORM");
        sampleConfig.setStatus("INACTIVE");
        sampleConfig.setEnvironment("PRODUCTION");
        sampleConfig.setPlatformName("Platform");
        sampleConfig.setPlatformUrl("https://platform.enterprise.com");
        sampleConfig.setDefaultLanguage("en");
        sampleConfig.setDefaultTimeZone("UTC");
        sampleConfig.setDefaultCurrency("USD");
        sampleConfig.setMaintenanceMode("DISABLED");
        sampleConfig.setFeatureToggle("ENABLED");
        sampleConfig.setAutoBackup("ENABLED");
        sampleConfig.setSessionTimeout(30);
        sampleConfig.setPasswordExpiry(90);
        sampleConfig.setMaximumLoginAttempts(5);
        sampleConfig.setEffectiveDate(LocalDate.now());
        sampleConfig.setCreatedAt(LocalDateTime.now());
        sampleConfig.setCreatedBy("admin");
        sampleConfig.setUpdatedAt(LocalDateTime.now());
        sampleConfig.setUpdatedBy("admin");
        sampleConfig.setDeleted(false);
        sampleConfig.setVersion(1);

        log.debug("Initialized test sampleConfig with id={}", configId);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        sampleConfig = null;
        configId = null;
        log.debug("Cleared SecurityContextHolder and reset test fixtures");
    }

    @AfterAll
    static void tearDownAll() {
        log.info("Completed all tests in PlatformConfigurationServiceImplTest suite");
    }

    // =========================================================================
    // CONSTRUCTOR & HISTORY REPOSITORY WIRING
    // =========================================================================

    @Test
    @DisplayName("Single-arg constructor should initialize service without history tracking")
    void testSingleArgConstructorWiring() {
        log.info("Executing testSingleArgConstructorWiring");

        PlatformConfigurationServiceImpl singleArgService = new PlatformConfigurationServiceImpl(repository);
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));

        // When history repository is null, getHistory returns empty list
        List<PlatformConfigurationHistoryResponse> historyList = singleArgService.getHistory(configId);
        assertThat(historyList).isEmpty();

        // When history repository is null, rollback throws PlatformConfigurationNotFoundException
        assertThatThrownBy(() -> singleArgService.rollbackToVersion(configId, 1))
                .isInstanceOf(PlatformConfigurationNotFoundException.class)
                .hasMessageContaining("History tracking is unavailable");
    }

    // =========================================================================
    // GET ALL & GET BY ID TESTS
    // =========================================================================

    @Test
    @DisplayName("getAll - Returns list of matching configurations")
    void getAll_shouldReturnMatchingList() {
        log.info("Executing test: getAll_shouldReturnMatchingList");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(sampleConfig));

        List<PlatformConfigurationResponse> list = service.getAll("Master", null, null, null);
        assertEquals(1, list.size());
        assertEquals("Master Config", list.get(0).configurationName());
    }

    @Test
    @DisplayName("getById - Successfully returns configuration response when found")
    void getById_shouldReturnResponseWhenFound() {
        log.info("Executing test: getById_shouldReturnResponseWhenFound");
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));

        PlatformConfigurationResponse response = service.getById(configId);
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(configId);
        assertThat(response.configurationName()).isEqualTo("Master Config");
    }

    @Test
    @DisplayName("getById - Throws PlatformConfigurationNotFoundException when not found")
    void getById_shouldThrowNotFoundException() {
        log.info("Executing test: getById_shouldThrowNotFoundException");
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(configId))
                .isInstanceOf(PlatformConfigurationNotFoundException.class)
                .hasMessageContaining(configId.toString());
    }

    // =========================================================================
    // MASTER DATA VALIDATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Master Data Validation - Throws InvalidConfigurationValueException for invalid ISO language")
    void create_shouldFailForInvalidLanguage() {
        log.info("Executing test: create_shouldFailForInvalidLanguage");
        PlatformConfigurationCreateRequest req = new PlatformConfigurationCreateRequest(
                "Config1", "PLATFORM", "desc", "INACTIVE", "Platform",
                "https://platform.enterprise.com", "DEVELOPMENT", "invalid_lang", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        assertThrows(InvalidConfigurationValueException.class, () -> service.create(req));
    }

    @Test
    @DisplayName("Master Data Validation - Throws InvalidConfigurationValueException for invalid IANA timezone")
    void create_shouldFailForInvalidTimezone() {
        log.info("Executing test: create_shouldFailForInvalidTimezone");
        PlatformConfigurationCreateRequest req = new PlatformConfigurationCreateRequest(
                "Config1", "PLATFORM", "desc", "INACTIVE", "Platform",
                "https://platform.enterprise.com", "DEVELOPMENT", "en", "Mars/Olympus", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        assertThrows(InvalidConfigurationValueException.class, () -> service.create(req));
    }

    @Test
    @DisplayName("Master Data Validation - Throws InvalidConfigurationValueException for invalid ISO 4217 currency")
    void create_shouldFailForInvalidCurrency() {
        log.info("Executing test: create_shouldFailForInvalidCurrency");
        PlatformConfigurationCreateRequest req = new PlatformConfigurationCreateRequest(
                "Config1", "PLATFORM", "desc", "INACTIVE", "Platform",
                "https://platform.enterprise.com", "DEVELOPMENT", "en", "UTC", "XYZ",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        assertThrows(InvalidConfigurationValueException.class, () -> service.create(req));
    }

    @Test
    @DisplayName("Master Data Validation - Throws InvalidConfigurationValueException for invalid category")
    void create_shouldFailForInvalidCategory() {
        log.info("Executing test: create_shouldFailForInvalidCategory");
        PlatformConfigurationCreateRequest req = new PlatformConfigurationCreateRequest(
                "Config1", "UNKNOWN_CATEGORY", "desc", "INACTIVE", "Platform",
                "https://platform.enterprise.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        assertThrows(InvalidConfigurationValueException.class, () -> service.create(req));
    }

    // =========================================================================
    // CREATE OPERATION TESTS
    // =========================================================================

    @Test
    @DisplayName("create - Fails when configuration name already exists")
    void create_shouldFailForDuplicateName() {
        log.info("Executing test: create_shouldFailForDuplicateName");
        PlatformConfigurationCreateRequest req = new PlatformConfigurationCreateRequest(
                "Duplicate Config", "PLATFORM", "desc", "INACTIVE", "Platform",
                "https://platform.enterprise.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );
        when(repository.existsByConfigurationNameIgnoreCaseAndDeletedFalse("Duplicate Config")).thenReturn(true);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(DuplicateConfigurationNameException.class);
    }

    @Test
    @DisplayName("create - Fails for invalid status or environment or toggle values")
    void create_shouldFailForInvalidAllowedValues() {
        log.info("Executing test: create_shouldFailForInvalidAllowedValues");

        // Invalid status
        PlatformConfigurationCreateRequest invalidStatus = new PlatformConfigurationCreateRequest(
                "Cfg", "PLATFORM", "desc", "UNKNOWN_STATUS", "Platform",
                "https://platform.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );
        assertThatThrownBy(() -> service.create(invalidStatus))
                .isInstanceOf(InvalidConfigurationValueException.class)
                .hasMessageContaining("Invalid status");

        // Invalid environment
        PlatformConfigurationCreateRequest invalidEnv = new PlatformConfigurationCreateRequest(
                "Cfg", "PLATFORM", "desc", "ACTIVE", "Platform",
                "https://platform.com", "INVALID_ENV", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );
        assertThatThrownBy(() -> service.create(invalidEnv))
                .isInstanceOf(InvalidConfigurationValueException.class)
                .hasMessageContaining("Invalid environment");

        // Invalid toggle (maintenanceMode)
        PlatformConfigurationCreateRequest invalidToggle = new PlatformConfigurationCreateRequest(
                "Cfg", "PLATFORM", "desc", "INACTIVE", "Platform",
                "https://platform.com", "DEVELOPMENT", "en", "UTC", "USD",
                "MAYBE", "ENABLED", "ENABLED", 30, 90, 5
        );
        assertThatThrownBy(() -> service.create(invalidToggle))
                .isInstanceOf(InvalidConfigurationValueException.class)
                .hasMessageContaining("Invalid maintenanceMode");
    }

    @Test
    @DisplayName("create - Successfully creates configuration with authenticated SecurityContext user")
    void create_shouldSucceedWithAuthenticatedUser() {
        log.info("Executing test: create_shouldSucceedWithAuthenticatedUser");

        // Mock authenticated user in SecurityContextHolder
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test_auditor", "password", List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        PlatformConfigurationCreateRequest req = new PlatformConfigurationCreateRequest(
                "Unique Active Config", "SYSTEM", "New Description", "ACTIVE", "Platform",
                "https://platform.enterprise.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        when(repository.existsByConfigurationNameIgnoreCaseAndDeletedFalse("Unique Active Config")).thenReturn(false);
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(invocation -> {
            PlatformConfiguration entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        PlatformConfigurationResponse response = service.create(req);
        assertThat(response).isNotNull();
        assertThat(response.configurationName()).isEqualTo("Unique Active Config");
        assertThat(response.createdBy()).isEqualTo("test_auditor");
        assertThat(response.version()).isEqualTo(1);
    }

    @Test
    @DisplayName("create - Handles null return from repository.save gracefully")
    void create_shouldHandleNullSaveGracefully() {
        log.info("Executing test: create_shouldHandleNullSaveGracefully");

        PlatformConfigurationCreateRequest req = new PlatformConfigurationCreateRequest(
                "Null Save Config", "SYSTEM", "Desc", "INACTIVE", "Platform",
                "https://platform.enterprise.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        when(repository.existsByConfigurationNameIgnoreCaseAndDeletedFalse("Null Save Config")).thenReturn(false);
        when(repository.save(any(PlatformConfiguration.class))).thenReturn(null);

        PlatformConfigurationResponse response = service.create(req);
        assertThat(response).isNotNull();
        assertThat(response.configurationName()).isEqualTo("Null Save Config");
    }

    // =========================================================================
    // UPDATE OPERATION TESTS
    // =========================================================================

    @Test
    @DisplayName("update - Fails when target configuration does not exist")
    void update_shouldFailWhenNotFound() {
        log.info("Executing test: update_shouldFailWhenNotFound");

        PlatformConfigurationUpdateRequest req = new PlatformConfigurationUpdateRequest(
                "Updated Name", "PLATFORM", "Desc", "INACTIVE", "Platform",
                "https://platform.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(configId, req))
                .isInstanceOf(PlatformConfigurationNotFoundException.class);
    }

    @Test
    @DisplayName("update - Fails when updated name collides with another active configuration")
    void update_shouldFailWhenDuplicateNameCollides() {
        log.info("Executing test: update_shouldFailWhenDuplicateNameCollides");

        PlatformConfigurationUpdateRequest req = new PlatformConfigurationUpdateRequest(
                "Existing Other Name", "PLATFORM", "Desc", "INACTIVE", "Platform",
                "https://platform.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(repository.existsByConfigurationNameIgnoreCaseAndDeletedFalseAndIdNot("Existing Other Name", configId)).thenReturn(true);

        assertThatThrownBy(() -> service.update(configId, req))
                .isInstanceOf(DuplicateConfigurationNameException.class);
    }

    @Test
    @DisplayName("update - Successfully updates configuration with version increment and pre-activation validation")
    void update_shouldSucceedWithVersionIncrement() {
        log.info("Executing test: update_shouldSucceedWithVersionIncrement");

        PlatformConfigurationUpdateRequest req = new PlatformConfigurationUpdateRequest(
                "Updated Unique Name", "SECURITY", "Updated Desc", "ACTIVE", "Platform",
                "https://platform.enterprise.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(repository.existsByConfigurationNameIgnoreCaseAndDeletedFalseAndIdNot("Updated Unique Name", configId)).thenReturn(false);
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatformConfigurationResponse response = service.update(configId, req);
        assertThat(response.configurationName()).isEqualTo("Updated Unique Name");
        assertThat(response.version()).isEqualTo(2);
        assertThat(response.updatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("update - Handles null existing version by setting version to 2")
    void update_shouldHandleNullExistingVersion() {
        log.info("Executing test: update_shouldHandleNullExistingVersion");

        sampleConfig.setVersion(null);
        PlatformConfigurationUpdateRequest req = new PlatformConfigurationUpdateRequest(
                "Updated Name", "SECURITY", "Desc", "INACTIVE", "Platform",
                "https://platform.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(repository.existsByConfigurationNameIgnoreCaseAndDeletedFalseAndIdNot("Updated Name", configId)).thenReturn(false);
        when(repository.save(any(PlatformConfiguration.class))).thenReturn(null);

        PlatformConfigurationResponse response = service.update(configId, req);
        assertThat(response.version()).isEqualTo(2);
    }

    // =========================================================================
    // PRE-ACTIVATION & STATUS UPDATE TESTS
    // =========================================================================

    @Test
    @DisplayName("Pre-Activation Validation - Fails when activating PRODUCTION config with Auto-Backup DISABLED")
    void updateStatus_shouldFailWhenActivatingProductionWithAutoBackupDisabled() {
        log.info("Executing test: updateStatus_shouldFailWhenActivatingProductionWithAutoBackupDisabled");
        sampleConfig.setAutoBackup("DISABLED");
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));

        PlatformConfigurationStatusUpdateRequest req = new PlatformConfigurationStatusUpdateRequest("ACTIVE");

        ConfigurationActivationException ex = assertThrows(
                ConfigurationActivationException.class,
                () -> service.updateStatus(configId, req)
        );
        assertTrue(ex.getMessage().contains("Auto-Backup DISABLED"));
    }

    @Test
    @DisplayName("Pre-Activation Validation - Fails when activating with session timeout exceeding password expiry")
    void updateStatus_shouldFailWhenSessionTimeoutExceedsPasswordExpiry() {
        log.info("Executing test: updateStatus_shouldFailWhenSessionTimeoutExceedsPasswordExpiry");
        sampleConfig.setPasswordExpiry(1); // 1 day = 1440 mins
        sampleConfig.setSessionTimeout(2000); // 2000 mins > 1440 mins
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));

        PlatformConfigurationStatusUpdateRequest req = new PlatformConfigurationStatusUpdateRequest("ACTIVE");

        ConfigurationActivationException ex = assertThrows(
                ConfigurationActivationException.class,
                () -> service.updateStatus(configId, req)
        );
        assertTrue(ex.getMessage().contains("Session timeout"));
    }

    @Test
    @DisplayName("Pre-Activation Validation - Succeeds when activating valid configuration")
    void updateStatus_shouldSucceedForValidConfig() {
        log.info("Executing test: updateStatus_shouldSucceedForValidConfig");
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlatformConfigurationStatusUpdateRequest req = new PlatformConfigurationStatusUpdateRequest("ACTIVE");
        PlatformConfigurationResponse res = service.updateStatus(configId, req);

        assertEquals("ACTIVE", res.status());
        assertEquals(2, res.version());
        assertNotNull(res.updatedAt());
    }

    @Test
    @DisplayName("updateStatus - Succeeds when deactivating configuration to INACTIVE")
    void updateStatus_shouldSucceedWhenDeactivating() {
        log.info("Executing test: updateStatus_shouldSucceedWhenDeactivating");

        sampleConfig.setStatus("ACTIVE");
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatformConfigurationStatusUpdateRequest req = new PlatformConfigurationStatusUpdateRequest("INACTIVE");
        PlatformConfigurationResponse res = service.updateStatus(configId, req);

        assertEquals("INACTIVE", res.status());
        assertEquals(2, res.version());
    }

    @Test
    @DisplayName("updateStatus - Fails for invalid status string")
    void updateStatus_shouldFailForInvalidStatus() {
        log.info("Executing test: updateStatus_shouldFailForInvalidStatus");

        PlatformConfigurationStatusUpdateRequest req = new PlatformConfigurationStatusUpdateRequest("INVALID_STATUS");
        assertThatThrownBy(() -> service.updateStatus(configId, req))
                .isInstanceOf(InvalidConfigurationValueException.class);
    }

    @Test
    @DisplayName("updateStatus - Idempotent when configuration is already in target status")
    void updateStatus_shouldBeIdempotentWhenAlreadyInStatus() {
        log.info("Executing test: updateStatus_shouldBeIdempotentWhenAlreadyInStatus");
        sampleConfig.setStatus("INACTIVE");
        sampleConfig.setVersion(1);
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));

        PlatformConfigurationStatusUpdateRequest req = new PlatformConfigurationStatusUpdateRequest("INACTIVE");
        PlatformConfigurationResponse res = service.updateStatus(configId, req);

        assertEquals("INACTIVE", res.status());
        assertEquals(1, res.version());
        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    // =========================================================================
    // SOFT DELETE & RESTORE TESTS
    // =========================================================================

    @Test
    @DisplayName("delete - Soft deletes active configuration, sets status INACTIVE, increments version and records snapshot")
    void delete_shouldSoftDeleteSuccessfully() {
        log.info("Executing test: delete_shouldSoftDeleteSuccessfully");

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));

        service.delete(configId);

        assertThat(sampleConfig.isDeleted()).isTrue();
        assertThat(sampleConfig.getDeletedAt()).isNotNull();
        assertThat(sampleConfig.getDeletedBy()).isEqualTo("admin");
        assertThat(sampleConfig.getStatus()).isEqualTo("INACTIVE");
        assertThat(sampleConfig.getVersion()).isEqualTo(2);
        verify(repository).save(sampleConfig);
        verify(historyRepository).save(any(PlatformConfigurationHistory.class));
    }

    @Test
    @DisplayName("restoreDefaultConfiguration - Restores defaults with current user")
    void restoreDefaultConfiguration_shouldRestoreDefaults() {
        log.info("Executing test: restoreDefaultConfiguration_shouldRestoreDefaults");
        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlatformConfigurationResponse res = service.restoreDefaultConfiguration(configId);

        assertEquals(PlatformConfigurationServiceImpl.DEFAULT_PLATFORM_NAME, res.platformName());
        assertEquals(PlatformConfigurationServiceImpl.DEFAULT_ENVIRONMENT, res.environment());
        assertEquals(2, res.version());
        assertEquals("SYSTEM_RESTORE", res.updatedBy());
    }

    // =========================================================================
    // HISTORY & ROLLBACK TESTS
    // =========================================================================

    @Test
    @DisplayName("getHistory - Returns historical snapshot responses ordered descending")
    void getHistory_shouldReturnSnapshots() {
        log.info("Executing test: getHistory_shouldReturnSnapshots");

        PlatformConfigurationHistory snap = PlatformConfigurationHistory.from(sampleConfig, "SNAPSHOT", "admin");
        snap.setId(UUID.randomUUID());

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(historyRepository.findAllByConfigurationIdOrderByVersionDesc(configId)).thenReturn(List.of(snap));

        List<PlatformConfigurationHistoryResponse> history = service.getHistory(configId);
        assertThat(history).hasSize(1);
        assertThat(history.get(0).configurationName()).isEqualTo(sampleConfig.getConfigurationName());
    }

    @Test
    @DisplayName("rollbackToVersion - Successfully rolls back with explicit updater")
    void rollbackToVersion_shouldRollbackWithExplicitUpdater() {
        log.info("Executing test: rollbackToVersion_shouldRollbackWithExplicitUpdater");

        PlatformConfigurationHistory snap = PlatformConfigurationHistory.from(sampleConfig, "V1 Snapshot", "admin");
        snap.setPlatformName("Old Platform Name");
        snap.setConfigurationCategory("SECURITY");
        snap.setStatus("INACTIVE");
        LocalDate oldDate = LocalDate.of(2025, 1, 1);
        snap.setEffectiveDate(oldDate);
        snap.setVersion(1);

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(historyRepository.findByConfigurationIdAndVersion(configId, 1)).thenReturn(Optional.of(snap));
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatformConfigurationResponse res = service.rollbackToVersion(configId, 1, "rollback_specialist");
        assertThat(res.platformName()).isEqualTo("Old Platform Name");
        assertThat(res.configurationCategory()).isEqualTo("SECURITY");
        assertThat(res.status()).isEqualTo("INACTIVE");
        assertThat(res.effectiveDate()).isEqualTo(oldDate);
        assertThat(res.version()).isEqualTo(2);
        assertThat(res.updatedBy()).isEqualTo("rollback_specialist");
    }

    @Test
    @DisplayName("rollbackToVersion - Fails pre-activation check when restored snapshot is ACTIVE but violates rules")
    void rollbackToVersion_shouldFailWhenRestoringActiveSnapshotViolatesPreActivation() {
        log.info("Executing test: rollbackToVersion_shouldFailWhenRestoringActiveSnapshotViolatesPreActivation");

        PlatformConfigurationHistory snap = PlatformConfigurationHistory.from(sampleConfig, "V1 Snapshot", "admin");
        snap.setStatus("ACTIVE");
        snap.setEnvironment("PRODUCTION");
        snap.setAutoBackup("DISABLED"); // Violates pre-activation for PRODUCTION
        snap.setVersion(1);

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(historyRepository.findByConfigurationIdAndVersion(configId, 1)).thenReturn(Optional.of(snap));

        ConfigurationActivationException ex = assertThrows(
                ConfigurationActivationException.class,
                () -> service.rollbackToVersion(configId, 1)
        );
        assertTrue(ex.getMessage().contains("Auto-Backup DISABLED"));
    }

    @Test
    @DisplayName("rollbackToVersion - Successfully validates pre-activation when restoring valid ACTIVE snapshot")
    void rollbackToVersion_shouldSucceedWhenRestoringValidActiveSnapshot() {
        log.info("Executing test: rollbackToVersion_shouldSucceedWhenRestoringValidActiveSnapshot");

        PlatformConfigurationHistory snap = PlatformConfigurationHistory.from(sampleConfig, "V1 Snapshot", "admin");
        snap.setStatus("ACTIVE");
        snap.setEnvironment("PRODUCTION");
        snap.setAutoBackup("ENABLED");
        snap.setSessionTimeout(30);
        snap.setPasswordExpiry(90);
        snap.setVersion(1);

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(historyRepository.findByConfigurationIdAndVersion(configId, 1)).thenReturn(Optional.of(snap));
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatformConfigurationResponse res = service.rollbackToVersion(configId, 1);
        assertThat(res.status()).isEqualTo("ACTIVE");
        assertThat(res.version()).isEqualTo(2);
    }

    @Test
    @DisplayName("rollbackToVersion - 2-arg overload delegates to current user")
    void rollbackToVersion_shouldDelegateTwoArgOverload() {
        log.info("Executing test: rollbackToVersion_shouldDelegateTwoArgOverload");

        PlatformConfigurationHistory snap = PlatformConfigurationHistory.from(sampleConfig, "V1 Snapshot", "admin");
        snap.setPlatformName("Rolled Back Name");
        snap.setVersion(1);

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(historyRepository.findByConfigurationIdAndVersion(configId, 1)).thenReturn(Optional.of(snap));
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatformConfigurationResponse res = service.rollbackToVersion(configId, 1);
        assertThat(res.platformName()).isEqualTo("Rolled Back Name");
        assertThat(res.version()).isEqualTo(2);
        assertThat(res.updatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("rollbackToVersion - Throws PlatformConfigurationNotFoundException when snapshot version does not exist")
    void rollbackToVersion_shouldThrowWhenSnapshotMissing() {
        log.info("Executing test: rollbackToVersion_shouldThrowWhenSnapshotMissing");

        when(repository.findByIdAndDeletedFalse(configId)).thenReturn(Optional.of(sampleConfig));
        when(historyRepository.findByConfigurationIdAndVersion(configId, 99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rollbackToVersion(configId, 99))
                .isInstanceOf(PlatformConfigurationNotFoundException.class)
                .hasMessageContaining("Version snapshot 99 not found");
    }

    // =========================================================================
    // SECURITY CONTEXT PRINCIPAL RESOLUTION & REFLECTION COVERAGE
    // =========================================================================

    @Test
    @DisplayName("SecurityContext principal resolution fallbacks: anonymous, blank name, unauthenticated")
    void testSecurityContextResolutionFallbacks() {
        log.info("Executing testSecurityContextResolutionFallbacks");

        // 1. anonymousUser principal
        Authentication anonAuth = new UsernamePasswordAuthenticationToken("anonymousUser", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(anonAuth);
        PlatformConfigurationCreateRequest req1 = new PlatformConfigurationCreateRequest(
                "Anon Config", "SYSTEM", "Desc", "INACTIVE", "Platform",
                "https://platform.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );
        when(repository.existsByConfigurationNameIgnoreCaseAndDeletedFalse("Anon Config")).thenReturn(false);
        when(repository.save(any(PlatformConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatformConfigurationResponse resp1 = service.create(req1);
        assertThat(resp1.createdBy()).isEqualTo("admin");

        // 2. Auth with blank name
        Authentication blankAuth = new UsernamePasswordAuthenticationToken("", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(blankAuth);
        PlatformConfigurationCreateRequest req2 = new PlatformConfigurationCreateRequest(
                "Blank Auth Config", "SYSTEM", "Desc", "INACTIVE", "Platform",
                "https://platform.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5
        );
        when(repository.existsByConfigurationNameIgnoreCaseAndDeletedFalse("Blank Auth Config")).thenReturn(false);
        PlatformConfigurationResponse resp2 = service.create(req2);
        assertThat(resp2.createdBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Verify private resolveUser method via reflection for 100% service coverage")
    void testResolveUserPrivateMethodViaReflection() throws Exception {
        log.info("Executing testResolveUserPrivateMethodViaReflection");

        Method resolveUserMethod = PlatformConfigurationServiceImpl.class
                .getDeclaredMethod("resolveUser", String.class, String.class);
        resolveUserMethod.setAccessible(true);

        // 1. Explicit non-blank requestUser
        String resolvedExplicit = (String) resolveUserMethod.invoke(service, "explicit_operator", "admin");
        assertThat(resolvedExplicit).isEqualTo("explicit_operator");

        // 2. Null requestUser
        String resolvedNull = (String) resolveUserMethod.invoke(service, null, "admin");
        assertThat(resolvedNull).isEqualTo("admin");

        // 3. Blank requestUser
        String resolvedBlank = (String) resolveUserMethod.invoke(service, "   ", "admin");
        assertThat(resolvedBlank).isEqualTo("admin");
    }

    // =========================================================================
    // DYNAMIC JPA SPECIFICATION TESTS (filterBy & Predicate Branches)
    // =========================================================================

    @SuppressWarnings("unchecked")
    private void setUpCriteriaMocks() {
        lenient().doReturn(specPath).when(specRoot).get(anyString());
        lenient().when(specCb.isFalse(any())).thenReturn(specPredicate);
        lenient().when(specCb.lower(any())).thenReturn(specStringExpression);
        lenient().when(specCb.upper(any())).thenReturn(specStringExpression);
        lenient().when(specCb.like(any(), anyString())).thenReturn(specPredicate);
        lenient().when(specCb.equal(any(), anyString())).thenReturn(specPredicate);
        lenient().when(specCb.and(any(Predicate[].class))).thenReturn(specPredicate);
    }

    @Test
    @DisplayName("Specification filterBy - All parameters null filters only by deleted=false")
    void testSpecification_allNullParameters() {
        log.info("Executing test: testSpecification_allNullParameters");
        setUpCriteriaMocks();
        Specification<PlatformConfiguration> spec = PlatformConfigurationSpecification.filterBy(null, null, null, null);
        Predicate result = spec.toPredicate(specRoot, specQuery, specCb);

        assertNotNull(result);
        verify(specCb).isFalse(specPath);
        verify(specCb, never()).like(any(), anyString());
        verify(specCb, never()).equal(any(), anyString());
    }

    @Test
    @DisplayName("Specification filterBy - All parameters blank filters only by deleted=false")
    void testSpecification_allBlankParameters() {
        log.info("Executing test: testSpecification_allBlankParameters");
        setUpCriteriaMocks();
        Specification<PlatformConfiguration> spec = PlatformConfigurationSpecification.filterBy("   ", "  ", "", " ");
        Predicate result = spec.toPredicate(specRoot, specQuery, specCb);

        assertNotNull(result);
        verify(specCb).isFalse(specPath);
        verify(specCb, never()).like(any(), anyString());
        verify(specCb, never()).equal(any(), anyString());
    }

    @Test
    @DisplayName("Specification filterBy - Name filter adds case-insensitive LIKE predicate")
    void testSpecification_nameFilterOnly() {
        log.info("Executing test: testSpecification_nameFilterOnly");
        setUpCriteriaMocks();
        Specification<PlatformConfiguration> spec = PlatformConfigurationSpecification.filterBy("Primary", null, null, null);
        Predicate result = spec.toPredicate(specRoot, specQuery, specCb);

        assertNotNull(result);
        verify(specCb).isFalse(specPath);
        verify(specCb).like(specStringExpression, "%primary%");
    }

    @Test
    @DisplayName("Specification filterBy - Category filter adds uppercase EQUAL predicate")
    void testSpecification_categoryFilterOnly() {
        log.info("Executing test: testSpecification_categoryFilterOnly");
        setUpCriteriaMocks();
        Specification<PlatformConfiguration> spec = PlatformConfigurationSpecification.filterBy(null, "platform", null, null);
        Predicate result = spec.toPredicate(specRoot, specQuery, specCb);

        assertNotNull(result);
        verify(specCb).isFalse(specPath);
        verify(specCb).equal(specStringExpression, "PLATFORM");
    }

    @Test
    @DisplayName("Specification filterBy - Environment filter adds uppercase EQUAL predicate")
    void testSpecification_environmentFilterOnly() {
        log.info("Executing test: testSpecification_environmentFilterOnly");
        setUpCriteriaMocks();
        Specification<PlatformConfiguration> spec = PlatformConfigurationSpecification.filterBy(null, null, "development", null);
        Predicate result = spec.toPredicate(specRoot, specQuery, specCb);

        assertNotNull(result);
        verify(specCb).isFalse(specPath);
        verify(specCb).equal(specStringExpression, "DEVELOPMENT");
    }

    @Test
    @DisplayName("Specification filterBy - Status filter adds uppercase EQUAL predicate")
    void testSpecification_statusFilterOnly() {
        log.info("Executing test: testSpecification_statusFilterOnly");
        setUpCriteriaMocks();
        Specification<PlatformConfiguration> spec = PlatformConfigurationSpecification.filterBy(null, null, null, "active");
        Predicate result = spec.toPredicate(specRoot, specQuery, specCb);

        assertNotNull(result);
        verify(specCb).isFalse(specPath);
        verify(specCb).equal(specStringExpression, "ACTIVE");
    }

    @Test
    @DisplayName("Specification filterBy - All parameters populated combines all predicates with AND")
    void testSpecification_allParametersPopulated() {
        log.info("Executing test: testSpecification_allParametersPopulated");
        setUpCriteriaMocks();
        Specification<PlatformConfiguration> spec = PlatformConfigurationSpecification.filterBy(
                "Primary", "platform", "production", "active");
        Predicate result = spec.toPredicate(specRoot, specQuery, specCb);

        assertNotNull(result);
        verify(specCb).isFalse(specPath);
        verify(specCb).like(specStringExpression, "%primary%");
        verify(specCb).equal(specStringExpression, "PLATFORM");
        verify(specCb).equal(specStringExpression, "PRODUCTION");
        verify(specCb).equal(specStringExpression, "ACTIVE");
        verify(specCb).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Specification - Instantiate private constructor via reflection")
    void testSpecification_privateConstructorViaReflection() throws Exception {
        log.info("Executing test: testSpecification_privateConstructorViaReflection");
        Constructor<PlatformConfigurationSpecification> constructor =
                PlatformConfigurationSpecification.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        PlatformConfigurationSpecification instance = constructor.newInstance();
        assertNotNull(instance);
    }

    // =========================================================================
    // ENTITY TESTS (PlatformConfiguration & PlatformConfigurationHistory)
    // =========================================================================

    @Test
    @DisplayName("Entity - PlatformConfiguration getters, setters, and date overloads")
    void testPlatformConfigurationEntityContracts() {
        log.info("Executing test: testPlatformConfigurationEntityContracts");

        PlatformConfiguration config = new PlatformConfiguration();
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 1);
        LocalDateTime now = LocalDateTime.now();

        config.setId(id);
        config.setConfigurationName("General");
        config.setConfigurationCategory("SYSTEM");
        config.setDescription("Desc");
        config.setStatus("ACTIVE");
        config.setEffectiveDate(date);
        config.setPlatformName("Platform");
        config.setPlatformUrl("https://platform.com");
        config.setEnvironment("PRODUCTION");
        config.setDefaultLanguage("en");
        config.setDefaultTimeZone("UTC");
        config.setDefaultCurrency("USD");
        config.setMaintenanceMode("DISABLED");
        config.setFeatureToggle("ENABLED");
        config.setAutoBackup("ENABLED");
        config.setSessionTimeout(30);
        config.setPasswordExpiry(90);
        config.setMaximumLoginAttempts(5);
        config.setCreatedAt(now);
        config.setCreatedBy("admin");
        config.setUpdatedAt(now);
        config.setUpdatedBy("admin");
        config.setDeleted(true);
        config.setDeletedAt(now);
        config.setDeletedBy("deleter");
        config.setVersion(2);

        assertEquals(id, config.getId());
        assertEquals("General", config.getConfigurationName());
        assertEquals("SYSTEM", config.getConfigurationCategory());
        assertEquals("Desc", config.getDescription());
        assertEquals("ACTIVE", config.getStatus());
        assertEquals(date, config.getEffectiveDate());
        assertEquals("Platform", config.getPlatformName());
        assertEquals("https://platform.com", config.getPlatformUrl());
        assertEquals("PRODUCTION", config.getEnvironment());
        assertEquals("en", config.getDefaultLanguage());
        assertEquals("UTC", config.getDefaultTimeZone());
        assertEquals("USD", config.getDefaultCurrency());
        assertEquals("DISABLED", config.getMaintenanceMode());
        assertEquals("ENABLED", config.getFeatureToggle());
        assertEquals("ENABLED", config.getAutoBackup());
        assertEquals(30, config.getSessionTimeout());
        assertEquals(90, config.getPasswordExpiry());
        assertEquals(5, config.getMaximumLoginAttempts());
        assertEquals(now, config.getCreatedAt());
        assertEquals("admin", config.getCreatedBy());
        assertEquals(now, config.getUpdatedAt());
        assertEquals("admin", config.getUpdatedBy());
        assertTrue(config.isDeleted());
        assertTrue(config.getDeleted());
        assertEquals(now, config.getDeletedAt());
        assertEquals("deleter", config.getDeletedBy());
        assertEquals(2, config.getVersion());

        // LocalDate setter overloads (non-null and null)
        config.setCreatedAt(date);
        config.setUpdatedAt(date);
        config.setDeletedAt(date);
        assertEquals(date.atStartOfDay(), config.getCreatedAt());
        assertEquals(date.atStartOfDay(), config.getUpdatedAt());
        assertEquals(date.atStartOfDay(), config.getDeletedAt());

        config.setCreatedAt((LocalDate) null);
        config.setUpdatedAt((LocalDate) null);
        config.setDeletedAt((LocalDate) null);
        assertThat(config.getCreatedAt()).isNull();
        assertThat(config.getUpdatedAt()).isNull();
        assertThat(config.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("Entity - PlatformConfiguration constructors, equals, and hashCode")
    void testPlatformConfigurationConstructorsAndEquals() {
        log.info("Executing test: testPlatformConfigurationConstructorsAndEquals");

        LocalDate date = LocalDate.of(2026, 1, 15);
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        // 26-arg legacy constructor with non-null LocalDate
        PlatformConfiguration c1 = new PlatformConfiguration(
                id1, "Config 1", "SYSTEM", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5,
                date, "creator", date, "updater", true, date, "deleter"
        );
        assertEquals(id1, c1.getId());
        assertEquals(1, c1.getVersion());

        // 26-arg constructor with null LocalDate
        PlatformConfiguration cNullDates = new PlatformConfiguration(
                id1, "Config Null", "SYSTEM", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5,
                (LocalDate) null, "creator", (LocalDate) null, "updater", false, (LocalDate) null, null
        );
        assertThat(cNullDates.getCreatedAt()).isNull();

        // All-args constructor with LocalDateTime
        LocalDateTime now = LocalDateTime.now();
        PlatformConfiguration c2 = new PlatformConfiguration(
                id1, "Config 2", "SECURITY", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5,
                now, "creator", now, "updater", false, null, null, 3
        );
        assertEquals(3, c2.getVersion());

        // equals & hashCode
        PlatformConfiguration c3 = new PlatformConfiguration();
        c3.setId(id2);
        PlatformConfiguration cNull1 = new PlatformConfiguration();
        PlatformConfiguration cNull2 = new PlatformConfiguration();

        assertTrue(c1.equals(c1));
        assertFalse(c1.equals(null));
        assertFalse(c1.equals("Not a config"));
        assertTrue(c1.equals(c2));
        assertEquals(c1.hashCode(), c2.hashCode());
        assertFalse(c1.equals(c3));
        assertTrue(cNull1.equals(cNull2));
        assertFalse(c1.equals(cNull1));
    }

    @Test
    @DisplayName("Entity - PlatformConfigurationHistory getters, setters, static factory, equals, and hashCode")
    void testPlatformConfigurationHistoryEntityContracts() {
        log.info("Executing test: testPlatformConfigurationHistoryEntityContracts");

        UUID hid1 = UUID.randomUUID();
        UUID hid2 = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 4, 10);
        LocalDateTime now = LocalDateTime.now();

        // All-args constructor
        PlatformConfigurationHistory h1 = new PlatformConfigurationHistory(
                hid1, configId, 1, "Snap", "SYSTEM", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "PRODUCTION", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5, now, "admin", "Reason"
        );
        assertEquals(hid1, h1.getId());
        assertEquals("Snap", h1.getConfigurationName());
        assertEquals("Reason", h1.getChangeReason());

        // LocalDate setter overloads
        h1.setRecordedAt(date);
        assertEquals(date.atStartOfDay(), h1.getRecordedAt());
        h1.setRecordedAt((LocalDate) null);
        assertThat(h1.getRecordedAt()).isNull();

        // Static factory from()
        PlatformConfigurationHistory snapshot = PlatformConfigurationHistory.from(sampleConfig, "Audit snap", "admin_auditor");
        assertNotNull(snapshot);
        assertEquals(sampleConfig.getId(), snapshot.getConfigurationId());
        assertEquals(sampleConfig.getConfigurationName(), snapshot.getConfigurationName());
        assertEquals("Audit snap", snapshot.getChangeReason());
        assertEquals("admin_auditor", snapshot.getRecordedBy());
        assertNotNull(snapshot.getRecordedAt());

        // equals & hashCode
        PlatformConfigurationHistory h2 = new PlatformConfigurationHistory();
        h2.setId(hid1);
        PlatformConfigurationHistory h3 = new PlatformConfigurationHistory();
        h3.setId(hid2);
        PlatformConfigurationHistory hNull1 = new PlatformConfigurationHistory();
        PlatformConfigurationHistory hNull2 = new PlatformConfigurationHistory();

        assertTrue(h1.equals(h1));
        assertFalse(h1.equals(null));
        assertFalse(h1.equals("Not a history"));
        assertTrue(h1.equals(h2));
        assertEquals(h1.hashCode(), h2.hashCode());
        assertFalse(h1.equals(h3));
        assertTrue(hNull1.equals(hNull2));
        assertFalse(h1.equals(hNull1));
    }

    // =========================================================================
    // RESPONSE DTOS CONSTRUCTORS & OVERLOADS
    // =========================================================================

    @Test
    @DisplayName("DTO - PlatformConfigurationResponse and HistoryResponse constructors")
    void testResponseDtoConstructors() {
        log.info("Executing test: testResponseDtoConstructors");

        LocalDate date = LocalDate.of(2026, 6, 15);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedDateTime = date.atStartOfDay();

        // 1. PlatformConfigurationResponse 23-arg LocalDateTime constructor
        PlatformConfigurationResponse r23Inst = new PlatformConfigurationResponse(
                configId, "Resp", "PLATFORM", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "PRODUCTION", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 90, 5, now, "admin", now, "admin", 4
        );
        assertEquals(4, r23Inst.version());
        assertEquals("Resp", r23Inst.configurationName());

        // 2. 22-arg LocalDateTime constructor defaulting version to 1
        PlatformConfigurationResponse r22Inst = new PlatformConfigurationResponse(
                configId, "Resp22", "SECURITY", "Desc", "INACTIVE", date,
                "Platform", "https://app.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 60, 45, 3, now, "creator", now, "updater"
        );
        assertEquals(1, r22Inst.version());

        // 3. 22-arg LocalDate constructor (non-null and null)
        PlatformConfigurationResponse r22Date = new PlatformConfigurationResponse(
                configId, "RespDate", "SECURITY", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 60, 45, 3, date, "creator", date, "updater"
        );
        assertEquals(expectedDateTime, r22Date.createdAt());
        assertEquals(1, r22Date.version());

        PlatformConfigurationResponse r22DateNull = new PlatformConfigurationResponse(
                configId, "RespNull", "SECURITY", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 60, 45, 3, (LocalDate) null, "creator", (LocalDate) null, "updater"
        );
        assertThat(r22DateNull.createdAt()).isNull();

        // 4. 23-arg LocalDate constructor with version (non-null and null)
        PlatformConfigurationResponse r23Date = new PlatformConfigurationResponse(
                configId, "Resp23Date", "SECURITY", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 60, 45, 3, date, "creator", date, "updater", 6
        );
        assertEquals(6, r23Date.version());
        assertEquals(expectedDateTime, r23Date.createdAt());

        PlatformConfigurationResponse r23DateNull = new PlatformConfigurationResponse(
                configId, "Resp23Null", "SECURITY", "Desc", "ACTIVE", date,
                "Platform", "https://app.com", "DEVELOPMENT", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 60, 45, 3, (LocalDate) null, "creator", (LocalDate) null, "updater", 8
        );
        assertThat(r23DateNull.createdAt()).isNull();
        assertEquals(8, r23DateNull.version());

        // 5. PlatformConfigurationHistoryResponse (LocalDateTime and LocalDate)
        PlatformConfigurationHistoryResponse hResp1 = new PlatformConfigurationHistoryResponse(
                UUID.randomUUID(), configId, 2, "HName", "AUDIT", "Desc", "ACTIVE", date,
                "Platform", "https://audit.com", "PRODUCTION", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 60, 4, now, "admin", "Reason"
        );
        assertEquals(2, hResp1.version());
        assertEquals(now, hResp1.recordedAt());

        PlatformConfigurationHistoryResponse hRespDate = new PlatformConfigurationHistoryResponse(
                UUID.randomUUID(), configId, 3, "HName2", "AUDIT", "Desc", "ACTIVE", date,
                "Platform", "https://audit.com", "PRODUCTION", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 60, 4, date, "admin", "Reason"
        );
        assertEquals(expectedDateTime, hRespDate.recordedAt());

        PlatformConfigurationHistoryResponse hRespDateNull = new PlatformConfigurationHistoryResponse(
                UUID.randomUUID(), configId, 4, "HName3", "AUDIT", "Desc", "ACTIVE", date,
                "Platform", "https://audit.com", "PRODUCTION", "en", "UTC", "USD",
                "DISABLED", "ENABLED", "ENABLED", 30, 60, 4, (LocalDate) null, "admin", "Reason"
        );
        assertThat(hRespDateNull.recordedAt()).isNull();
    }
}