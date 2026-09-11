package com.enterprise.superadmin.platformconfiguration.api;

import com.enterprise.superadmin.platformconfiguration.entity.PlatformConfiguration;
import com.enterprise.superadmin.platformconfiguration.entity.PlatformConfigurationHistory;
import com.enterprise.superadmin.platformconfiguration.repository.PlatformConfigurationHistoryRepository;
import com.enterprise.superadmin.platformconfiguration.repository.PlatformConfigurationRepository;
import com.enterprise.superadmin.platformconfiguration.service.PlatformConfigurationServiceImpl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end API and database integration test suite for Platform Configuration
 * endpoints.
 *
 * <p>
 * <b>FRS Traceability:</b>
 * </p>
 * <ul>
 * <li><b>Module:</b> Module 1 &mdash; Platform Administration (FR-001.2
 * Platform Configuration)</li>
 * <li><b>Rules:</b> VAL-0007 (Unique Name), VAL-0008 (Valid URL), VAL-0010
 * (Positive Timeout), VAL-0011 (Positive Attempts)</li>
 * <li><b>Acceptance Criteria:</b> AC-0009 (Create/Update), AC-0010
 * (Validation), AC-0015 (Defaults), AC-0016 (Consistency)</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "SUPER_ADMIN")
@Transactional
class PlatformConfigurationApiTest {

	private static final Logger log = LoggerFactory.getLogger(PlatformConfigurationApiTest.class);

	// =========================================================================
	// STATIC TEST FIXTURES
	// =========================================================================
	private static String namespace;
	private static String configName;
	private static String duplicateConfigName;
	private static String updatedConfigName;
	private static String deletedConfigName;
	private static String category;
	private static String description;
	private static String platformName;
	private static String platformUrl;
	private static String defaultLang;
	private static String defaultTz;
	private static String defaultCurr;
	private static String createdBy;
	private static String updatedBy;
	private static String validCreateJson;
	private static String validUpdateJson;
	private static String validStatusJson;

	// =========================================================================
	// SPRING COMPONENTS
	// =========================================================================
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PlatformConfigurationRepository repository;

	@Autowired
	private PlatformConfigurationHistoryRepository historyRepository;

	// =========================================================================
	// LIFECYCLE CALLBACKS
	// =========================================================================
	@BeforeAll
	static void beforeAll() {
		log.info("Initializing static test fixtures and JSON templates for PlatformConfigurationApiTest");

		namespace = "api-" + UUID.randomUUID().toString().substring(0, 8);
		configName = namespace + "-PrimaryConfig";
		duplicateConfigName = namespace + "-DuplicateConfig";
		updatedConfigName = namespace + "-UpdatedConfig";
		deletedConfigName = namespace + "-DeletedConfig";

		category = "PLATFORM";
		description = "Full stack platform configuration test";
		platformName = "One Enterprise Cloud Platform";
		platformUrl = "https://platform.enterprise.com";
		defaultLang = "en";
		defaultTz = "UTC";
		defaultCurr = "USD";
		createdBy = "admin";
		updatedBy = "admin";

		validCreateJson = """
				{
				  "configurationName": "%s",
				  "configurationCategory": "%s",
				  "description": "%s",
				  "status": "ACTIVE",
				  "platformName": "%s",
				  "platformUrl": "%s",
				  "environment": "DEVELOPMENT",
				  "defaultLanguage": "%s",
				  "defaultTimeZone": "%s",
				  "defaultCurrency": "%s",
				  "maintenanceMode": "DISABLED",
				  "featureToggle": "ENABLED",
				  "autoBackup": "ENABLED",
				  "sessionTimeout": 30,
				  "passwordExpiry": 90,
				  "maximumLoginAttempts": 5
				}
				""".formatted(configName, category, description, platformName, platformUrl, defaultLang, defaultTz,
				defaultCurr);

		validUpdateJson = """
				{
				  "configurationName": "%s",
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
				""".formatted(updatedConfigName);

		validStatusJson = """
				{
				  "status": "INACTIVE"
				}
				""";
	}

	@BeforeEach
	void beforeEach() {
		log.debug("Purging configurations table prior to test run for namespace: {}", namespace);
		repository.deleteAllInBatch();
		repository.flush();
	}

	@AfterEach
	void afterEach() {
		log.debug("Cleaning database state after test execution for namespace: {}", namespace);
		repository.deleteAllInBatch();
		repository.flush();
	}

	@AfterAll
	static void afterAll() {
		log.info("Tearing down static fixtures for PlatformConfigurationApiTest");
		namespace = null;
		configName = null;
		duplicateConfigName = null;
		updatedConfigName = null;
		deletedConfigName = null;
		category = null;
		description = null;
		platformName = null;
		platformUrl = null;
		defaultLang = null;
		defaultTz = null;
		defaultCurr = null;
		createdBy = null;
		updatedBy = null;
		validCreateJson = null;
		validUpdateJson = null;
		validStatusJson = null;
	}

	// =========================================================================
	// GET ALL CONFIGURATIONS (FR-001.2)
	// =========================================================================
	@Test
	@DisplayName("GET /api/v1/platform-configurations - Returns only non-deleted configurations (HTTP 200)")
	void getAll_shouldReturnOnlyActiveNonDeletedConfigurations() throws Exception {
		log.info("Executing API Test: getAll_shouldReturnOnlyActiveNonDeletedConfigurations");

		PlatformConfiguration active = repository.saveAndFlush(createTestEntity(configName, false));
		PlatformConfiguration deleted = repository.saveAndFlush(createTestEntity(deletedConfigName, true));

		mockMvc.perform(get("/api/v1/platform-configurations").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(active.getId().toString()))
				.andExpect(jsonPath("$[0].configurationName").value(configName));
	}

	@Test
	@DisplayName("GET /api/v1/platform-configurations - Returns empty list when no configurations exist (HTTP 200)")
	void getAll_shouldReturnEmptyArray_whenNoneExist() throws Exception {
		log.info("Executing API Test: getAll_shouldReturnEmptyArray_whenNoneExist");

		mockMvc.perform(get("/api/v1/platform-configurations").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	@DisplayName("GET /api/v1/platform-configurations with query filters - Returns matching configurations")
	void getAll_withFilters_shouldReturnMatchingConfigurations() throws Exception {
		log.info("Executing API Test: getAll_withFilters_shouldReturnMatchingConfigurations");

		PlatformConfiguration active = repository.saveAndFlush(createTestEntity(configName, false));

		// Filter by matching name
		mockMvc.perform(get("/api/v1/platform-configurations").param("name", configName).param("category", "PLATFORM")
				.param("environment", "DEVELOPMENT").param("status", "ACTIVE").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(active.getId().toString()));

		// Filter by non-matching name
		mockMvc.perform(get("/api/v1/platform-configurations").param("name", "NonExistentName")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	// =========================================================================
	// GET BY ID (FR-001.2)
	// =========================================================================
	@Test
	@DisplayName("GET /api/v1/platform-configurations/{id} - Returns complete configuration by ID (HTTP 200)")
	void getById_shouldReturnConfiguration_whenActive() throws Exception {
		log.info("Executing API Test: getById_shouldReturnConfiguration_whenActive");

		PlatformConfiguration saved = repository.saveAndFlush(createTestEntity(configName, false));

		mockMvc.perform(get("/api/v1/platform-configurations/{id}", saved.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(saved.getId().toString()))
				.andExpect(jsonPath("$.configurationName").value(configName))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.platformName").value(platformName))
				.andExpect(jsonPath("$.platformUrl").value(platformUrl))
				.andExpect(jsonPath("$.sessionTimeout").value(30)).andExpect(jsonPath("$.passwordExpiry").value(90))
				.andExpect(jsonPath("$.maximumLoginAttempts").value(5));
	}

	@Test
	@DisplayName("GET /api/v1/platform-configurations/{id} - Returns 404 NOT FOUND for non-existent ID")
	void getById_shouldReturn404_whenIdDoesNotExist() throws Exception {
		log.info("Executing API Test: getById_shouldReturn404_whenIdDoesNotExist");

		mockMvc.perform(
				get("/api/v1/platform-configurations/{id}", UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@DisplayName("GET /api/v1/platform-configurations/{id} - Returns 404 NOT FOUND for soft-deleted configuration")
	void getById_shouldReturn404_whenConfigurationIsSoftDeleted() throws Exception {
		log.info("Executing API Test: getById_shouldReturn404_whenConfigurationIsSoftDeleted");

		PlatformConfiguration deleted = repository.saveAndFlush(createTestEntity(deletedConfigName, true));

		mockMvc.perform(get("/api/v1/platform-configurations/{id}", deleted.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
	}

	// =========================================================================
	// POST - CREATE (FR-001.2, AC-0009, VAL-0007, ERR-0007)
	// =========================================================================
	@Test
	@DisplayName("POST /api/v1/platform-configurations - Creates and persists configuration (HTTP 201)")
	void create_shouldPersistAndReturn201_whenValid() throws Exception {
		log.info("Executing API Test: create_shouldPersistAndReturn201_whenValid");

		mockMvc.perform(post("/api/v1/platform-configurations").contentType(MediaType.APPLICATION_JSON)
				.content(validCreateJson)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.configurationName").value(configName))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.platformName").value(platformName));

		PlatformConfiguration persisted = repository.findAllByDeletedFalse().stream()
				.filter(c -> configName.equals(c.getConfigurationName())).findFirst().orElse(null);

		assertNotNull(persisted, "Configuration must be persisted in database");
		assertFalse(persisted.getDeleted());
		assertEquals(30, persisted.getSessionTimeout());
		assertEquals(90, persisted.getPasswordExpiry());
		assertEquals(5, persisted.getMaximumLoginAttempts());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Successfully creates configuration with null description")
	void create_shouldAllowNullDescription() throws Exception {
		log.info("Executing API Test: create_shouldAllowNullDescription");

		String nullDescJson = """
				{
				  "configurationName": "%s-NoDesc",
				  "configurationCategory": "PLATFORM",
				  "description": null,
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
				""".formatted(configName);

		mockMvc.perform(
				post("/api/v1/platform-configurations").contentType(MediaType.APPLICATION_JSON).content(nullDescJson))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.description").doesNotExist());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 400 BAD REQUEST on invalid URL (VAL-0008, ERR-0008)")
	void create_shouldReturn400_whenUrlIsInvalid() throws Exception {
		log.info("Executing API Test: create_shouldReturn400_whenUrlIsInvalid");

		String invalidUrlJson = """
				{
				  "configurationName": "%s-BadUrl",
				  "configurationCategory": "PLATFORM",
				  "description": "Desc",
				  "status": "ACTIVE",
				  "platformName": "Platform",
				  "platformUrl": "ht!tp://invalid-url",
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
				""".formatted(configName);

		mockMvc.perform(
				post("/api/v1/platform-configurations").contentType(MediaType.APPLICATION_JSON).content(invalidUrlJson))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.validationErrors.platformUrl").exists());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 400 BAD REQUEST on non-positive numeric fields (VAL-0010, VAL-0011)")
	void create_shouldReturn400_whenNumericFieldsZeroOrNegative() throws Exception {
		log.info("Executing API Test: create_shouldReturn400_whenNumericFieldsZeroOrNegative");

		String invalidNumbersJson = """
				{
				  "configurationName": "%s-BadNumbers",
				  "configurationCategory": "PLATFORM",
				  "description": "Desc",
				  "status": "ACTIVE",
				  "platformName": "Platform",
				  "platformUrl": "https://platform.com",
				  "environment": "DEVELOPMENT",
				  "defaultLanguage": "en",
				  "defaultTimeZone": "UTC",
				  "defaultCurrency": "USD",
				  "maintenanceMode": "DISABLED",
				  "featureToggle": "ENABLED",
				  "autoBackup": "ENABLED",
				  "sessionTimeout": 0,
				  "passwordExpiry": -10,
				  "maximumLoginAttempts": 0
				}
				""".formatted(configName);

		mockMvc.perform(post("/api/v1/platform-configurations").contentType(MediaType.APPLICATION_JSON)
				.content(invalidNumbersJson)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.sessionTimeout").exists())
				.andExpect(jsonPath("$.validationErrors.passwordExpiry").exists())
				.andExpect(jsonPath("$.validationErrors.maximumLoginAttempts").exists());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 409 CONFLICT on duplicate configuration name (VAL-0007, ERR-0007)")
	void create_shouldReturn409_whenConfigurationNameAlreadyExists() throws Exception {
		log.info("Executing API Test: create_shouldReturn409_whenConfigurationNameAlreadyExists");

		repository.saveAndFlush(createTestEntity(configName, false));

		mockMvc.perform(post("/api/v1/platform-configurations").contentType(MediaType.APPLICATION_JSON)
				.content(validCreateJson)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 409 CONFLICT on case-insensitive duplicate name")
	void create_shouldReturn409_whenConfigurationNameMatchesCaseInsensitive() throws Exception {
		log.info("Executing API Test: create_shouldReturn409_whenConfigurationNameMatchesCaseInsensitive");

		repository.saveAndFlush(createTestEntity(configName.toLowerCase(), false));

		String uppercasePayload = validCreateJson.replace(configName, configName.toUpperCase());

		mockMvc.perform(post("/api/v1/platform-configurations").contentType(MediaType.APPLICATION_JSON)
				.content(uppercasePayload)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.errorCode").value("ERR-0007"));
	}

	// =========================================================================
	// PUT - UPDATE (FR-001.2, AC-0009)
	// =========================================================================
	@Test
	@DisplayName("PUT /api/v1/platform-configurations/{id} - Modifies all fields and persists to database (HTTP 200)")
	void update_shouldModifyDatabaseRecord_whenRequestIsValid() throws Exception {
		log.info("Executing API Test: update_shouldModifyDatabaseRecord_whenRequestIsValid");

		PlatformConfiguration initial = repository.saveAndFlush(createTestEntity(configName, false));

		mockMvc.perform(put("/api/v1/platform-configurations/{id}", initial.getId())
				.contentType(MediaType.APPLICATION_JSON).content(validUpdateJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(initial.getId().toString()))
				.andExpect(jsonPath("$.configurationName").value(updatedConfigName))
				.andExpect(jsonPath("$.status").value("INACTIVE")).andExpect(jsonPath("$.environment").value("STAGING"))
				.andExpect(jsonPath("$.defaultCurrency").value("EUR"));

		PlatformConfiguration reloaded = repository.findById(initial.getId()).orElseThrow();
		assertEquals(updatedConfigName, reloaded.getConfigurationName());
		assertEquals("INACTIVE", reloaded.getStatus());
		assertEquals(60, reloaded.getSessionTimeout());
		assertEquals(180, reloaded.getPasswordExpiry());
		assertEquals(3, reloaded.getMaximumLoginAttempts());
	}

	@Test
	@DisplayName("PUT /api/v1/platform-configurations/{id} - Allows updating while retaining the existing name")
	void update_shouldSucceed_whenRetainingSameName() throws Exception {
		log.info("Executing API Test: update_shouldSucceed_whenRetainingSameName");

		PlatformConfiguration initial = repository.saveAndFlush(createTestEntity(configName, false));

		String sameNameJson = """
				{
				  "configurationName": "%s",
				  "configurationCategory": "PLATFORM",
				  "description": "Retaining current name with modified timeout",
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
				  "sessionTimeout": 45,
				  "passwordExpiry": 90,
				  "maximumLoginAttempts": 5,
				  "updatedBy": "admin"
				}
				""".formatted(configName);

		mockMvc.perform(put("/api/v1/platform-configurations/{id}", initial.getId())
				.contentType(MediaType.APPLICATION_JSON).content(sameNameJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.configurationName").value(configName))
				.andExpect(jsonPath("$.sessionTimeout").value(45));
	}

	@Test
	@DisplayName("PUT /api/v1/platform-configurations/{id} - Returns 409 CONFLICT if name belongs to another configuration")
	void update_shouldReturn409_whenNameCollidesWithAnotherConfiguration() throws Exception {
		log.info("Executing API Test: update_shouldReturn409_whenNameCollidesWithAnotherConfiguration");

		PlatformConfiguration config1 = repository.saveAndFlush(createTestEntity(configName, false));
		PlatformConfiguration config2 = repository.saveAndFlush(createTestEntity(duplicateConfigName, false));

		String collidingJson = """
				{
				  "configurationName": "%s",
				  "configurationCategory": "PLATFORM",
				  "description": "Colliding update",
				  "status": "ACTIVE",
				  "platformName": "Platform",
				  "platformUrl": "https://platform.com",
				  "environment": "DEVELOPMENT",
				  "defaultLanguage": "en",
				  "defaultTimeZone": "UTC",
				  "defaultCurrency": "USD",
				  "maintenanceMode": "DISABLED",
				  "featureToggle": "ENABLED",
				  "autoBackup": "ENABLED",
				  "sessionTimeout": 30,
				  "passwordExpiry": 90,
				  "maximumLoginAttempts": 5,
				  "updatedBy": "admin"
				}
				""".formatted(configName);

		mockMvc.perform(put("/api/v1/platform-configurations/{id}", config2.getId())
				.contentType(MediaType.APPLICATION_JSON).content(collidingJson)).andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	@DisplayName("PUT /api/v1/platform-configurations/{id} - Returns 404 NOT FOUND for non-existent configuration")
	void update_shouldReturn404_whenIdDoesNotExist() throws Exception {
		log.info("Executing API Test: update_shouldReturn404_whenIdDoesNotExist");

		mockMvc.perform(put("/api/v1/platform-configurations/{id}", UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON).content(validUpdateJson)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	// =========================================================================
	// PATCH - UPDATE STATUS (FR-001.2, AC-0016)
	// =========================================================================
	@Test
	@DisplayName("PATCH /api/v1/platform-configurations/{id}/status - Modifies configuration status (HTTP 200)")
	void updateStatus_shouldModifyStatusInDatabase() throws Exception {
		log.info("Executing API Test: updateStatus_shouldModifyStatusInDatabase");

		PlatformConfiguration initial = repository.saveAndFlush(createTestEntity(configName, false));

		mockMvc.perform(patch("/api/v1/platform-configurations/{id}/status", initial.getId())
				.contentType(MediaType.APPLICATION_JSON).content(validStatusJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(initial.getId().toString()))
				.andExpect(jsonPath("$.status").value("INACTIVE"));

		PlatformConfiguration updated = repository.findById(initial.getId()).orElseThrow();
		assertEquals("INACTIVE", updated.getStatus());
	}

	@Test
	@DisplayName("PATCH /api/v1/platform-configurations/{id}/status - Returns 404 NOT FOUND when ID missing")
	void updateStatus_shouldReturn404_whenIdMissing() throws Exception {
		log.info("Executing API Test: updateStatus_shouldReturn404_whenIdMissing");

		mockMvc.perform(patch("/api/v1/platform-configurations/{id}/status", UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON).content(validStatusJson)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	// =========================================================================
	// DELETE - SOFT DELETE (FR-001.2)
	// =========================================================================
	@Test
	@DisplayName("DELETE /api/v1/platform-configurations/{id} - Sets is_deleted=true in database (HTTP 204)")
	void delete_shouldPerformSoftDelete() throws Exception {
		log.info("Executing API Test: delete_shouldPerformSoftDelete");

		PlatformConfiguration saved = repository.saveAndFlush(createTestEntity(configName, false));

		mockMvc.perform(delete("/api/v1/platform-configurations/{id}", saved.getId()))
				.andExpect(status().isNoContent());

		PlatformConfiguration databaseRecord = repository.findById(saved.getId()).orElseThrow();
		assertTrue(databaseRecord.getDeleted(), "Database record must be marked deleted");
		assertNotNull(databaseRecord.getDeletedAt(), "deletedAt date must be recorded");
		assertEquals("admin", databaseRecord.getDeletedBy(), "deletedBy must be populated with authenticated user");
		assertEquals("INACTIVE", databaseRecord.getStatus(), "Soft-deleted record must have status INACTIVE");
		assertEquals(2, databaseRecord.getVersion(), "Soft-deleted record must increment version");
		assertTrue(repository.findByIdAndDeletedFalse(saved.getId()).isEmpty());
	}

	@Test
	@DisplayName("DELETE /api/v1/platform-configurations/{id} - Returns 404 NOT FOUND when already deleted or non-existent")
	void delete_shouldReturn404_whenAlreadyDeleted() throws Exception {
		log.info("Executing API Test: delete_shouldReturn404_whenAlreadyDeleted");

		PlatformConfiguration alreadyDeleted = repository.saveAndFlush(createTestEntity(deletedConfigName, true));

		mockMvc.perform(delete("/api/v1/platform-configurations/{id}", alreadyDeleted.getId()))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
	}

	// =========================================================================
	// POST - RESTORE DEFAULTS (FR-001.2, AWF-001.2.3, AC-0015, NTF-0014)
	// =========================================================================
	@Test
	@DisplayName("POST /api/v1/platform-configurations/{id}/restore-default - Resets all fields while preserving identity and status (HTTP 200)")
	void restoreDefaultConfiguration_shouldResetAllFieldsToDefaults() throws Exception {
		log.info("Executing API Test: restoreDefaultConfiguration_shouldResetAllFieldsToDefaults");

		PlatformConfiguration custom = createTestEntity(configName, false);
		custom.setPlatformName("Custom Platform");
		custom.setPlatformUrl("https://custom.url.com");
		custom.setEnvironment("STAGING");
		custom.setDefaultLanguage("de");
		custom.setDefaultTimeZone("America/New_York");
		custom.setDefaultCurrency("EUR");
		custom.setMaintenanceMode("ENABLED");
		custom.setFeatureToggle("DISABLED");
		custom.setAutoBackup("DISABLED");
		custom.setSessionTimeout(120);
		custom.setPasswordExpiry(365);
		custom.setMaximumLoginAttempts(10);
		custom.setStatus("ACTIVE");

		PlatformConfiguration saved = repository.saveAndFlush(custom);

		mockMvc.perform(post("/api/v1/platform-configurations/{id}/restore-default", saved.getId()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(saved.getId().toString()))
				.andExpect(jsonPath("$.configurationName").value(configName))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.platformName").value(PlatformConfigurationServiceImpl.DEFAULT_PLATFORM_NAME))
				.andExpect(jsonPath("$.platformUrl").value(PlatformConfigurationServiceImpl.DEFAULT_PLATFORM_URL))
				.andExpect(jsonPath("$.environment").value(PlatformConfigurationServiceImpl.DEFAULT_ENVIRONMENT))
				.andExpect(jsonPath("$.defaultLanguage").value(PlatformConfigurationServiceImpl.DEFAULT_LANGUAGE))
				.andExpect(jsonPath("$.defaultTimeZone").value(PlatformConfigurationServiceImpl.DEFAULT_TIME_ZONE))
				.andExpect(jsonPath("$.defaultCurrency").value(PlatformConfigurationServiceImpl.DEFAULT_CURRENCY))
				.andExpect(jsonPath("$.maintenanceMode")
						.value(PlatformConfigurationServiceImpl.DEFAULT_MAINTENANCE_MODE))
				.andExpect(jsonPath("$.featureToggle")
						.value(PlatformConfigurationServiceImpl.DEFAULT_FEATURE_TOGGLE))
				.andExpect(jsonPath("$.autoBackup").value(PlatformConfigurationServiceImpl.DEFAULT_AUTO_BACKUP))
				.andExpect(jsonPath("$.sessionTimeout").value(PlatformConfigurationServiceImpl.DEFAULT_SESSION_TIMEOUT))
				.andExpect(jsonPath("$.passwordExpiry").value(PlatformConfigurationServiceImpl.DEFAULT_PASSWORD_EXPIRY))
				.andExpect(jsonPath("$.maximumLoginAttempts")
						.value(PlatformConfigurationServiceImpl.DEFAULT_MAX_LOGIN_ATTEMPTS))
				.andExpect(jsonPath("$.updatedBy").value("admin"));

		PlatformConfiguration databaseRecord = repository.findById(saved.getId()).orElseThrow();
		assertEquals(PlatformConfigurationServiceImpl.DEFAULT_SESSION_TIMEOUT, databaseRecord.getSessionTimeout());
		assertEquals(PlatformConfigurationServiceImpl.DEFAULT_PASSWORD_EXPIRY, databaseRecord.getPasswordExpiry());
		assertEquals(PlatformConfigurationServiceImpl.DEFAULT_MAX_LOGIN_ATTEMPTS,
				databaseRecord.getMaximumLoginAttempts());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations/{id}/restore-default - Returns 404 NOT FOUND for missing configuration")
	void restoreDefaultConfiguration_shouldReturn404_whenIdMissing() throws Exception {
		log.info("Executing API Test: restoreDefaultConfiguration_shouldReturn404_whenIdMissing");

		mockMvc.perform(post("/api/v1/platform-configurations/{id}/restore-default", UUID.randomUUID()))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@DisplayName("GET /api/v1/platform-configurations/{id}/history - Successfully retrieves audit history snapshots")
	void getHistory_shouldReturnHistorySnapshots() throws Exception {
		log.info("Executing API Test: getHistory_shouldReturnHistorySnapshots");

		PlatformConfiguration entity = repository.save(createTestEntity(namespace + "-HistConfig", false));

		// Update to generate a 2nd version and snapshot
		String updateJson = validUpdateJson.formatted(namespace + "-HistConfig", "UPDATED", "Updated Desc");
		mockMvc.perform(put("/api/v1/platform-configurations/{id}", entity.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(updateJson))
				.andExpect(status().isOk());

		// Retrieve history
		mockMvc.perform(get("/api/v1/platform-configurations/{id}/history", entity.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
				.andExpect(jsonPath("$[0].version").value(2));
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations/{id}/rollback/{version} - Successfully rolls back to snapshot version")
	void rollback_shouldRestorePreviousVersion() throws Exception {
		log.info("Executing API Test: rollback_shouldRestorePreviousVersion");

		PlatformConfiguration entity = repository.save(createTestEntity(namespace + "-RollbackConfig", false));
		historyRepository.save(PlatformConfigurationHistory.from(entity, "INITIAL_CREATION", "admin"));
		UUID configId = entity.getId();

		// Update to change platform name
		String updateJson = validUpdateJson.formatted(namespace + "-RollbackConfig", "UPDATED", "Updated Desc");
		mockMvc.perform(put("/api/v1/platform-configurations/{id}", configId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(updateJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.platformName").value("Updated Platform"))
				.andExpect(jsonPath("$.version").value(2));

		// Roll back to version 1
		mockMvc.perform(post("/api/v1/platform-configurations/{id}/rollback/{version}", configId, 1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.platformName").value(platformName))
				.andExpect(jsonPath("$.version").value(3));
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations/{id}/rollback/{version} - Returns 404 when target version missing")
	void rollback_shouldReturn404_whenTargetVersionNotFound() throws Exception {
		log.info("Executing API Test: rollback_shouldReturn404_whenTargetVersionNotFound");

		PlatformConfiguration entity = repository.save(createTestEntity(namespace + "-RollbackMiss", false));

		mockMvc.perform(post("/api/v1/platform-configurations/{id}/rollback/{version}", entity.getId(), 999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Request is permitted under permitAll configuration")
	void create_shouldSucceed_whenPermitAllConfigured() throws Exception {
		log.info("Executing API Test: create_shouldSucceed_whenPermitAllConfigured");

		String payload = validCreateJson.formatted(
				namespace + "-PermitAll", category, description,
				platformName, platformUrl, defaultLang, defaultTz, defaultCurr
		);

		mockMvc.perform(post("/api/v1/platform-configurations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isCreated());
	}

	// =========================================================================
	// AUDIT USER AUTO-POPULATION INTEGRATION TESTS
	// =========================================================================
	@Test
	@DisplayName("POST /api/v1/platform-configurations - Automatically populates createdBy and updatedBy from SecurityContext when omitted")
	void create_shouldAutoPopulateAuditFieldsFromSecurityContext_whenOmittedInPayload() throws Exception {
		log.info("Executing API Test: create_shouldAutoPopulateAuditFieldsFromSecurityContext_whenOmittedInPayload");
		String targetName = namespace + "-AutoAuditCreate";
		String payload = """
				{
				  "configurationName": "%s",
				  "configurationCategory": "PLATFORM",
				  "description": "Auto audit create test",
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
				""".formatted(targetName);

		mockMvc.perform(post("/api/v1/platform-configurations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.createdBy").value("admin"))
				.andExpect(jsonPath("$.updatedBy").value("admin"));

		PlatformConfiguration persisted = repository.findAllByDeletedFalse().stream()
				.filter(c -> targetName.equals(c.getConfigurationName())).findFirst().orElse(null);
		assertNotNull(persisted);
		assertEquals("admin", persisted.getCreatedBy());
		assertEquals("admin", persisted.getUpdatedBy());
	}

	@Test
	@DisplayName("PUT /api/v1/platform-configurations/{id} - Automatically populates updatedBy from SecurityContext when omitted")
	void update_shouldAutoPopulateUpdatedByFromSecurityContext_whenOmittedInPayload() throws Exception {
		log.info("Executing API Test: update_shouldAutoPopulateUpdatedByFromSecurityContext_whenOmittedInPayload");
		PlatformConfiguration initial = repository.saveAndFlush(createTestEntity(namespace + "-AutoAuditUpdate", false));

		String payload = """
				{
				  "configurationName": "%s",
				  "configurationCategory": "PLATFORM",
				  "description": "Auto audit update test",
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
				""".formatted(initial.getConfigurationName());

		mockMvc.perform(put("/api/v1/platform-configurations/{id}", initial.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.updatedBy").value("admin"));

		PlatformConfiguration updated = repository.findById(initial.getId()).orElseThrow();
		assertEquals("admin", updated.getUpdatedBy());
	}

	@Test
	@DisplayName("PATCH /api/v1/platform-configurations/{id}/status - Automatically populates updatedBy from SecurityContext when omitted")
	void updateStatus_shouldAutoPopulateUpdatedByFromSecurityContext_whenOmittedInPayload() throws Exception {
		log.info("Executing API Test: updateStatus_shouldAutoPopulateUpdatedByFromSecurityContext_whenOmittedInPayload");
		PlatformConfiguration initial = repository.saveAndFlush(createTestEntity(namespace + "-AutoAuditStatus", false));

		String payload = """
				{
				  "status": "INACTIVE"
				}
				""";

		mockMvc.perform(patch("/api/v1/platform-configurations/{id}/status", initial.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.updatedBy").value("admin"));

		PlatformConfiguration updated = repository.findById(initial.getId()).orElseThrow();
		assertEquals("admin", updated.getUpdatedBy());
		assertEquals("INACTIVE", updated.getStatus());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 400 BAD REQUEST when status is invalid")
	void create_shouldReturn400_whenStatusIsInvalid() throws Exception {
		log.info("Executing API Test: create_shouldReturn400_whenStatusIsInvalid");
		String payload = """
				{
				  "configurationName": "%s-BadStatus",
				  "configurationCategory": "PLATFORM",
				  "status": "INVALID_STATUS",
				  "platformName": "Platform",
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
				""".formatted(configName);

		mockMvc.perform(post("/api/v1/platform-configurations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.status").exists());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 400 BAD REQUEST when environment is invalid")
	void create_shouldReturn400_whenEnvironmentIsInvalid() throws Exception {
		log.info("Executing API Test: create_shouldReturn400_whenEnvironmentIsInvalid");
		String payload = """
				{
				  "configurationName": "%s-BadEnv",
				  "configurationCategory": "PLATFORM",
				  "status": "ACTIVE",
				  "platformName": "Platform",
				  "platformUrl": "https://platform.enterprise.com",
				  "environment": "UNKNOWN_ENV",
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
				""".formatted(configName);

		mockMvc.perform(post("/api/v1/platform-configurations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.environment").exists());
	}

	@Test
	@DisplayName("PATCH /api/v1/platform-configurations/{id}/status - Returns 400 BAD REQUEST when status is invalid")
	void updateStatus_shouldReturn400_whenStatusIsInvalid() throws Exception {
		log.info("Executing API Test: updateStatus_shouldReturn400_whenStatusIsInvalid");
		PlatformConfiguration initial = repository.saveAndFlush(createTestEntity(namespace + "-InvalidStatusPatch", false));

		String payload = """
				{
				  "status": "NON_EXISTENT_STATUS"
				}
				""";

		mockMvc.perform(patch("/api/v1/platform-configurations/{id}/status", initial.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.status").exists());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations/{id}/rollback/{version} - Automatically populates updatedBy from SecurityContext when param omitted")
	void rollback_shouldAutoPopulateUpdatedByFromSecurityContext_whenParamOmitted() throws Exception {
		log.info("Executing API Test: rollback_shouldAutoPopulateUpdatedByFromSecurityContext_whenParamOmitted");
		PlatformConfiguration entity = repository.saveAndFlush(createTestEntity(namespace + "-AutoAuditRollback", false));

		// Record a v1 snapshot in historyRepository
		PlatformConfigurationHistory snapshot = PlatformConfigurationHistory.from(entity, "INITIAL_CREATION", "admin");
		historyRepository.saveAndFlush(snapshot);

		mockMvc.perform(post("/api/v1/platform-configurations/{id}/rollback/{version}", entity.getId(), 1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.updatedBy").value("admin"));

		PlatformConfiguration rolledBack = repository.findById(entity.getId()).orElseThrow();
		assertEquals("admin", rolledBack.getUpdatedBy());
	}

	// =========================================================================
	// VALIDATION & EXCEPTION TESTS (FR-001.2, VAL-0007 through VAL-0011)
	// =========================================================================

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 400 BAD REQUEST when status fails validation")
	void create_shouldReturn400_whenStatusPatternFailsDiscontinued() throws Exception {
		log.info("Executing API Test: create_shouldReturn400_whenStatusPatternFailsDiscontinued");
		String payload = """
				{
				  "configurationName": "%s-InvalidStatus",
				  "configurationCategory": "PLATFORM",
				  "description": "Validation test description",
				  "status": "DISCONTINUED",
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
				""".formatted(configName);

		mockMvc.perform(post("/api/v1/platform-configurations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.validationErrors.status").exists());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 400 BAD REQUEST when maintenanceMode is invalid")
	void create_shouldReturn400_whenMaintenanceModeIsInvalid() throws Exception {
		log.info("Executing API Test: create_shouldReturn400_whenMaintenanceModeIsInvalid");
		String payload = """
				{
				  "configurationName": "%s-InvalidToggle",
				  "configurationCategory": "PLATFORM",
				  "description": "Validation test description",
				  "status": "ACTIVE",
				  "platformName": "One Enterprise Cloud Platform",
				  "platformUrl": "https://platform.enterprise.com",
				  "environment": "DEVELOPMENT",
				  "defaultLanguage": "en",
				  "defaultTimeZone": "UTC",
				  "defaultCurrency": "USD",
				  "maintenanceMode": "PARTIAL",
				  "featureToggle": "ENABLED",
				  "autoBackup": "ENABLED",
				  "sessionTimeout": 30,
				  "passwordExpiry": 90,
				  "maximumLoginAttempts": 5
				}
				""".formatted(configName);

		mockMvc.perform(post("/api/v1/platform-configurations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.validationErrors.maintenanceMode").exists());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 400 BAD REQUEST when timeout integer is non-positive")
	void create_shouldReturn400_whenSessionTimeoutIsZeroOrNegative() throws Exception {
		log.info("Executing API Test: create_shouldReturn400_whenSessionTimeoutIsZeroOrNegative");
		String payload = """
				{
				  "configurationName": "%s-InvalidTimeout",
				  "configurationCategory": "PLATFORM",
				  "description": "Validation test description",
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
				  "sessionTimeout": 0,
				  "passwordExpiry": 90,
				  "maximumLoginAttempts": 5
				}
				""".formatted(configName);

		mockMvc.perform(post("/api/v1/platform-configurations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.validationErrors.sessionTimeout").exists());
	}

	@Test
	@DisplayName("PUT /api/v1/platform-configurations/{id} - Returns 400 BAD REQUEST when environment fails validation")
	void update_shouldReturn400_whenEnvironmentIsInvalid() throws Exception {
		log.info("Executing API Test: update_shouldReturn400_whenEnvironmentIsInvalid");
		PlatformConfiguration initial = repository.saveAndFlush(createTestEntity(namespace + "-InvalidEnvUpdate", false));

		String payload = """
				{
				  "configurationName": "%s-UpdatedName",
				  "configurationCategory": "PLATFORM",
				  "description": "Updated Description",
				  "status": "ACTIVE",
				  "platformName": "One Enterprise Cloud Platform",
				  "platformUrl": "https://platform.enterprise.com",
				  "environment": "ON_PREMISE_DEV",
				  "defaultLanguage": "en",
				  "defaultTimeZone": "UTC",
				  "defaultCurrency": "USD",
				  "maintenanceMode": "DISABLED",
				  "featureToggle": "ENABLED",
				  "autoBackup": "ENABLED",
				  "sessionTimeout": 60,
				  "passwordExpiry": 90,
				  "maximumLoginAttempts": 5
				}
				""".formatted(configName);

		mockMvc.perform(put("/api/v1/platform-configurations/{id}", initial.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.validationErrors.environment").exists());
	}

	@Test
	@DisplayName("POST /api/v1/platform-configurations - Returns 400 BAD REQUEST when request body is malformed JSON")
	void handleUnreadableJson_shouldReturn400() throws Exception {
		log.info("Executing API Test: handleUnreadableJson_shouldReturn400");
		String malformedPayload = "{ \"configurationName\": ";

		mockMvc.perform(post("/api/v1/platform-configurations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(malformedPayload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Invalid request body."));
	}

	@Test
	@DisplayName("GET /api/v1/platform-configurations/{id} - Returns 404 NOT FOUND when target record is soft-deleted")
	void getById_shouldReturn404_whenDeletedConfigurationRequested() throws Exception {
		log.info("Executing API Test: getById_shouldReturn404_whenDeletedConfigurationRequested");
		PlatformConfiguration deletedRecord = repository.saveAndFlush(createTestEntity(namespace + "-SoftDeletedQuery", true));

		mockMvc.perform(get("/api/v1/platform-configurations/{id}", deletedRecord.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"));
	}

	// =========================================================================
	// HELPER METHODS
	// =========================================================================
	private PlatformConfiguration createTestEntity(String name, boolean isDeleted) {
		LocalDate today = LocalDate.now();
		return new PlatformConfiguration(null, name, category, description, "ACTIVE", today,
				platformName, platformUrl, "DEVELOPMENT", defaultLang, defaultTz, defaultCurr,
				"DISABLED", "ENABLED", "ENABLED", 30, 90, 5,
				today, createdBy, today, updatedBy, isDeleted, isDeleted ? today : null,
				isDeleted ? "admin" : null);
	}
}