package com.enterprise.superadmin.validation;

import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationCreateRequest;
import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationStatusUpdateRequest;
import com.enterprise.superadmin.platformconfiguration.dto.request.PlatformConfigurationUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dedicated Bean Validation test suite for Platform Configuration request DTOs.
 *
 * <p>Validates Jakarta Bean Validation annotations ({@code @NotBlank}, {@code @Pattern},
 * {@code @Size}, {@code @URL}, {@code @Positive}, {@code @NotNull}) independently of web and service layers.</p>
 */
@DisplayName("PlatformConfiguration Request DTO Validation Tests")
class PlatformConfigurationValidationTest {

    private static final Logger log = LoggerFactory.getLogger(PlatformConfigurationValidationTest.class);

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        log.info("Initializing Jakarta Bean Validation ValidatorFactory");
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        log.debug("Preparing validation test fixtures before test execution");
    }

    @AfterEach
    void tearDown() {
        log.debug("Cleaning up per-test validation fixtures after test execution");
    }

    @AfterAll
    static void tearDownAll() {
        log.info("Closing ValidatorFactory and releasing validation resources");
        if (factory != null) {
            factory.close();
            factory = null;
        }
        validator = null;
    }

    private PlatformConfigurationCreateRequest createValidCreatePayload() {
        return new PlatformConfigurationCreateRequest(
                "Primary-Platform-Configuration",
                "PLATFORM",
                "Primary operational platform configuration",
                "ACTIVE",
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
                5
        );
    }

    // =========================================================================
    // CREATE REQUEST VALIDATIONS
    // =========================================================================

    @Test
    @DisplayName("CreateRequest - Passes validation when all fields are valid")
    void createRequest_shouldHaveZeroViolations_whenPayloadIsValid() {
        log.info("Executing test: createRequest_shouldHaveZeroViolations_whenPayloadIsValid");
        PlatformConfigurationCreateRequest request = createValidCreatePayload();
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Expected zero violations for valid create request");
    }

    @Test
    @DisplayName("CreateRequest - Passes validation when optional audit fields are omitted")
    void createRequest_shouldPass_whenAuditFieldsOmitted() {
        log.info("Executing test: createRequest_shouldPass_whenAuditFieldsOmitted");
        PlatformConfigurationCreateRequest request = new PlatformConfigurationCreateRequest(
                "Primary-Config", "PLATFORM", "Description", "ACTIVE",
                "Platform Name", "https://platform.enterprise.com", "PRODUCTION",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                30, 90, 5
        );
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Audit fields must be optional in request DTO");
    }

    @Test
    @DisplayName("CreateRequest - Fails validation when configurationName is blank or null")
    void createRequest_shouldFail_whenConfigurationNameBlank() {
        log.info("Executing test: createRequest_shouldFail_whenConfigurationNameBlank");
        PlatformConfigurationCreateRequest request = new PlatformConfigurationCreateRequest(
                "", "PLATFORM", "Description", "ACTIVE",
                "Platform Name", "https://platform.enterprise.com", "DEVELOPMENT",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                30, 90, 5
        );
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "configurationName".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("CreateRequest - Fails validation when configurationName exceeds 100 characters")
    void createRequest_shouldFail_whenConfigurationNameExceedsLength() {
        log.info("Executing test: createRequest_shouldFail_whenConfigurationNameExceedsLength");
        String longName = "A".repeat(101);
        PlatformConfigurationCreateRequest request = new PlatformConfigurationCreateRequest(
                longName, "PLATFORM", "Description", "ACTIVE",
                "Platform Name", "https://platform.enterprise.com", "DEVELOPMENT",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                30, 90, 5
        );
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "configurationName".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("CreateRequest - Fails validation when status does not match ACTIVE or INACTIVE")
    void createRequest_shouldFail_whenStatusPatternInvalid() {
        log.info("Executing test: createRequest_shouldFail_whenStatusPatternInvalid");
        PlatformConfigurationCreateRequest request = new PlatformConfigurationCreateRequest(
                "Primary-Config", "PLATFORM", "Description", "DISCONTINUED",
                "Platform Name", "https://platform.enterprise.com", "DEVELOPMENT",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                30, 90, 5
        );
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "status".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("CreateRequest - Fails validation when environment does not match allowed tiers")
    void createRequest_shouldFail_whenEnvironmentPatternInvalid() {
        log.info("Executing test: createRequest_shouldFail_whenEnvironmentPatternInvalid");
        PlatformConfigurationCreateRequest request = new PlatformConfigurationCreateRequest(
                "Primary-Config", "PLATFORM", "Description", "ACTIVE",
                "Platform Name", "https://platform.enterprise.com", "LOCAL_SANDBOX",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                30, 90, 5
        );
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "environment".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("CreateRequest - Fails validation when toggles do not match ENABLED or DISABLED")
    void createRequest_shouldFail_whenTogglePatternInvalid() {
        log.info("Executing test: createRequest_shouldFail_whenTogglePatternInvalid");
        PlatformConfigurationCreateRequest request = new PlatformConfigurationCreateRequest(
                "Primary-Config", "PLATFORM", "Description", "ACTIVE",
                "Platform Name", "https://platform.enterprise.com", "DEVELOPMENT",
                "en", "UTC", "USD", "MAYBE", "ENABLED", "ENABLED",
                30, 90, 5
        );
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "maintenanceMode".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("CreateRequest - Fails validation when platformUrl is not a valid URL")
    void createRequest_shouldFail_whenUrlInvalid() {
        log.info("Executing test: createRequest_shouldFail_whenUrlInvalid");
        PlatformConfigurationCreateRequest request = new PlatformConfigurationCreateRequest(
                "Primary-Config", "PLATFORM", "Description", "ACTIVE",
                "Platform Name", "invalid-web-address", "DEVELOPMENT",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                30, 90, 5
        );
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "platformUrl".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("CreateRequest - Fails validation when numeric fields are zero or negative")
    void createRequest_shouldFail_whenNumericFieldsNonPositive() {
        log.info("Executing test: createRequest_shouldFail_whenNumericFieldsNonPositive");
        PlatformConfigurationCreateRequest request = new PlatformConfigurationCreateRequest(
                "Primary-Config", "PLATFORM", "Description", "ACTIVE",
                "Platform Name", "https://platform.enterprise.com", "DEVELOPMENT",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                0, -10, 0
        );
        Set<ConstraintViolation<PlatformConfigurationCreateRequest>> violations = validator.validate(request);
        assertEquals(3, violations.size());
        assertTrue(violations.stream().anyMatch(v -> "sessionTimeout".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "passwordExpiry".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "maximumLoginAttempts".equals(v.getPropertyPath().toString())));
    }

    // =========================================================================
    // UPDATE REQUEST VALIDATIONS
    // =========================================================================

    @Test
    @DisplayName("UpdateRequest - Passes validation when all fields are valid")
    void updateRequest_shouldHaveZeroViolations_whenPayloadIsValid() {
        log.info("Executing test: updateRequest_shouldHaveZeroViolations_whenPayloadIsValid");
        PlatformConfigurationUpdateRequest request = new PlatformConfigurationUpdateRequest(
                "Updated-Configuration",
                "SYSTEM",
                "Updated description",
                "INACTIVE",
                "Updated Platform Name",
                "https://updated.platform.enterprise.com",
                "STAGING",
                "fr",
                "Europe/Paris",
                "EUR",
                "ENABLED",
                "DISABLED",
                "DISABLED",
                60,
                180,
                3
        );
        Set<ConstraintViolation<PlatformConfigurationUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Expected zero violations for valid update request");
    }

    @Test
    @DisplayName("UpdateRequest - Fails validation when status is invalid")
    void updateRequest_shouldFail_whenStatusInvalid() {
        log.info("Executing test: updateRequest_shouldFail_whenStatusInvalid");
        PlatformConfigurationUpdateRequest request = new PlatformConfigurationUpdateRequest(
                "Updated-Configuration", "SYSTEM", "Description", "UNKNOWN_STATUS",
                "Platform Name", "https://platform.enterprise.com", "STAGING",
                "en", "UTC", "USD", "DISABLED", "ENABLED", "ENABLED",
                30, 90, 5
        );
        Set<ConstraintViolation<PlatformConfigurationUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "status".equals(v.getPropertyPath().toString())));
    }

    // =========================================================================
    // STATUS UPDATE REQUEST VALIDATIONS
    // =========================================================================

    @Test
    @DisplayName("StatusUpdateRequest - Passes validation for ACTIVE and INACTIVE")
    void statusUpdateRequest_shouldPass_whenStatusIsValid() {
        log.info("Executing test: statusUpdateRequest_shouldPass_whenStatusIsValid");
        PlatformConfigurationStatusUpdateRequest activeRequest = new PlatformConfigurationStatusUpdateRequest("ACTIVE");
        PlatformConfigurationStatusUpdateRequest inactiveRequest = new PlatformConfigurationStatusUpdateRequest("INACTIVE");

        assertTrue(validator.validate(activeRequest).isEmpty());
        assertTrue(validator.validate(inactiveRequest).isEmpty());
    }

    @Test
    @DisplayName("StatusUpdateRequest - Fails validation when status is blank or invalid pattern")
    void statusUpdateRequest_shouldFail_whenStatusIsInvalidOrBlank() {
        log.info("Executing test: statusUpdateRequest_shouldFail_whenStatusIsInvalidOrBlank");
        PlatformConfigurationStatusUpdateRequest blankRequest = new PlatformConfigurationStatusUpdateRequest("   ");
        PlatformConfigurationStatusUpdateRequest invalidRequest = new PlatformConfigurationStatusUpdateRequest("PENDING");

        assertFalse(validator.validate(blankRequest).isEmpty());
        assertFalse(validator.validate(invalidRequest).isEmpty());
    }
}
