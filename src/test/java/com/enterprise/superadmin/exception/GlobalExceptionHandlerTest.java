package com.enterprise.superadmin.exception;

import com.enterprise.superadmin.platformconfiguration.exception.ApiError;
import com.enterprise.superadmin.platformconfiguration.exception.ConfigurationActivationException;
import com.enterprise.superadmin.platformconfiguration.exception.ConfigurationImportException;
import com.enterprise.superadmin.platformconfiguration.exception.ConfigurationRepositoryUnavailableException;
import com.enterprise.superadmin.platformconfiguration.exception.DuplicateConfigurationNameException;
import com.enterprise.superadmin.platformconfiguration.exception.GlobalExceptionHandler;
import com.enterprise.superadmin.platformconfiguration.exception.InvalidConfigurationValueException;
import com.enterprise.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException;
import com.enterprise.superadmin.platformconfiguration.exception.UnauthorizedConfigurationAccessException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Dedicated unit test suite for {@link GlobalExceptionHandler}.
 *
 * <p>Validates that domain and framework exceptions are translated into standardized
 * {@link ApiError} response structures with appropriate HTTP status codes, FRS error codes,
 * and sanitized messages.</p>
 *
 * <p>Implements standard JUnit 5 lifecycle hooks ({@code @BeforeAll}, {@code @BeforeEach},
 * {@code @AfterEach}, {@code @AfterAll}) with comprehensive SLF4J audit logging.</p>
 */
@DisplayName("GlobalExceptionHandler Comprehensive Unit Tests")
class GlobalExceptionHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandlerTest.class);

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeAll
    static void setUpAll() {
        log.info("Starting GlobalExceptionHandlerTest suite execution");
    }

    @BeforeEach
    void setUp() {
        log.debug("Initializing clean GlobalExceptionHandler and MockHttpServletRequest");
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/v1/platform-configurations/test");
    }

    @AfterEach
    void tearDown() {
        log.debug("Tearing down test request and handler references");
        handler = null;
        request = null;
    }

    @AfterAll
    static void tearDownAll() {
        log.info("Completed GlobalExceptionHandlerTest suite execution. Static fixtures cleared.");
    }

    @Test
    @DisplayName("handleNotFound - Returns 404 NOT FOUND with ERR-0011 for PlatformConfigurationNotFoundException")
    void handleNotFound_shouldReturn404() {
        log.info("Executing test: handleNotFound_shouldReturn404");
        PlatformConfigurationNotFoundException ex = new PlatformConfigurationNotFoundException("Configuration not found: 123");

        ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("ERR-0011", response.getBody().errorCode());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Configuration not found: 123", response.getBody().message());
        assertEquals("/api/v1/platform-configurations/test", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
        assertTrue(response.getBody().validationErrors().isEmpty());
    }

    @Test
    @DisplayName("handleDuplicateConfiguration - Returns 409 CONFLICT with ERR-0007 for DuplicateConfigurationNameException")
    void handleDuplicateConfiguration_shouldReturn409() {
        log.info("Executing test: handleDuplicateConfiguration_shouldReturn409");
        DuplicateConfigurationNameException ex = new DuplicateConfigurationNameException("Duplicate name: Primary");

        ResponseEntity<ApiError> response = handler.handleDuplicateConfiguration(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("ERR-0007", response.getBody().errorCode());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Duplicate name: Primary", response.getBody().message());
        assertEquals("/api/v1/platform-configurations/test", response.getBody().path());
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - Returns 409 CONFLICT with ERR-0007 for DataIntegrityViolationException")
    void handleDataIntegrityViolation_shouldReturn409() {
        log.info("Executing test: handleDataIntegrityViolation_shouldReturn409");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("violates unique constraint uq_platform_config_active_name");

        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("ERR-0007", response.getBody().errorCode());
        assertEquals("Conflict", response.getBody().error());
        assertThat(response.getBody().message()).contains("already exists or violates database integrity constraints");
    }

    @Test
    @DisplayName("handleInvalidConfigurationValue - Returns 400 BAD REQUEST with ERR-0008 for InvalidConfigurationValueException")
    void handleInvalidConfigurationValue_shouldReturn400() {
        log.info("Executing test: handleInvalidConfigurationValue_shouldReturn400");
        InvalidConfigurationValueException ex = new InvalidConfigurationValueException("Invalid status: 'ARCHIVED'");

        ResponseEntity<ApiError> response = handler.handleInvalidConfigurationValue(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("ERR-0008", response.getBody().errorCode());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Invalid status: 'ARCHIVED'", response.getBody().message());
    }

    @Test
    @DisplayName("handleConfigurationActivation - Returns 400 BAD REQUEST with ERR-0009 for ConfigurationActivationException")
    void handleConfigurationActivation_shouldReturn400() {
        log.info("Executing test: handleConfigurationActivation_shouldReturn400");
        ConfigurationActivationException ex = new ConfigurationActivationException("Failed to activate configuration");

        ResponseEntity<ApiError> response = handler.handleConfigurationActivation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("ERR-0009", response.getBody().errorCode());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Failed to activate configuration", response.getBody().message());
    }

    @Test
    @DisplayName("handleConfigurationImport - Returns 400 BAD REQUEST with ERR-0010 for ConfigurationImportException")
    void handleConfigurationImport_shouldReturn400() {
        log.info("Executing test: handleConfigurationImport_shouldReturn400");
        ConfigurationImportException ex = new ConfigurationImportException("Invalid file format during import");

        ResponseEntity<ApiError> response = handler.handleConfigurationImport(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("ERR-0010", response.getBody().errorCode());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Invalid file format during import", response.getBody().message());
    }

    @Test
    @DisplayName("handleUnauthorizedAccess - Returns 403 FORBIDDEN with ERR-0006 for UnauthorizedConfigurationAccessException")
    void handleUnauthorizedAccess_shouldReturn403() {
        log.info("Executing test: handleUnauthorizedAccess_shouldReturn403");
        UnauthorizedConfigurationAccessException ex = new UnauthorizedConfigurationAccessException("Super admin role required");

        ResponseEntity<ApiError> response = handler.handleUnauthorizedAccess(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("ERR-0006", response.getBody().errorCode());
        assertEquals("Forbidden", response.getBody().error());
        assertEquals("Super admin role required", response.getBody().message());
    }

    @Test
    @DisplayName("handleRepositoryUnavailable - Returns 503 SERVICE UNAVAILABLE with ERR-0012 for ConfigurationRepositoryUnavailableException")
    void handleRepositoryUnavailable_shouldReturn503() {
        log.info("Executing test: handleRepositoryUnavailable_shouldReturn503");
        ConfigurationRepositoryUnavailableException ex = new ConfigurationRepositoryUnavailableException("Database cluster unreachable");

        ResponseEntity<ApiError> response = handler.handleRepositoryUnavailable(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("ERR-0012", response.getBody().errorCode());
        assertEquals("Service Unavailable", response.getBody().error());
        assertEquals("Database cluster unreachable", response.getBody().message());
    }

    @Test
    @DisplayName("handleValidation - Returns 400 BAD REQUEST with field errors map for MethodArgumentNotValidException")
    void handleValidation_shouldReturn400WithValidationErrors() {
        log.info("Executing test: handleValidation_shouldReturn400WithValidationErrors");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "status", "Status must be either ACTIVE or INACTIVE"));
        bindingResult.addError(new FieldError("request", "environment", "Environment is required"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("ERR-0008", response.getBody().errorCode());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Request validation failed", response.getBody().message());

        Map<String, String> fieldErrors = response.getBody().validationErrors();
        assertEquals(2, fieldErrors.size());
        assertEquals("Status must be either ACTIVE or INACTIVE", fieldErrors.get("status"));
        assertEquals("Environment is required", fieldErrors.get("environment"));
    }

    @Test
    @DisplayName("handleUnreadableRequest - Returns 400 with 'Invalid request body.' when cause is generic")
    void handleUnreadableRequest_genericCause() {
        log.info("Executing test: handleUnreadableRequest_genericCause");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Malformed JSON", new RuntimeException("Syntax error"), new MockHttpInputMessage(new byte[0])
        );

        ResponseEntity<ApiError> response = handler.handleUnreadableRequest(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("ERR-0008", response.getBody().errorCode());
        assertEquals("Invalid request body.", response.getBody().message());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("handleUnreadableRequest - Resolves field name when cause is InvalidFormatException on Enum")
    void handleUnreadableRequest_enumFormatExceptionWithField() {
        log.info("Executing test: handleUnreadableRequest_enumFormatExceptionWithField");

        InvalidFormatException ife = mock(InvalidFormatException.class);
        when(ife.getTargetType()).thenReturn((Class) RetentionPolicy.class);

        tools.jackson.databind.deser.impl.NullsConstantProvider.class.getClassLoader();
        tools.jackson.databind.exc.InvalidFormatException.Reference ref = mock(tools.jackson.databind.exc.InvalidFormatException.Reference.class);
        when(ref.getPropertyName()).thenReturn("environment");
        when(ife.getPath()).thenReturn(List.of(ref));

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid enum", ife, new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ApiError> response = handler.handleUnreadableRequest(ex, request);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value for environment.", response.getBody().message());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("handleUnreadableRequest - Fallback field name 'request field' when path is empty or null")
    void handleUnreadableRequest_enumFormatExceptionWithEmptyPath() {
        log.info("Executing test: handleUnreadableRequest_enumFormatExceptionWithEmptyPath");

        InvalidFormatException ife = mock(InvalidFormatException.class);
        when(ife.getTargetType()).thenReturn((Class) RetentionPolicy.class);
        when(ife.getPath()).thenReturn(Collections.emptyList());

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid enum", ife, new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ApiError> response = handler.handleUnreadableRequest(ex, request);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value for request field.", response.getBody().message());
    }

    @Test
    @DisplayName("handleResourceNotFound - Returns 404 NOT FOUND for NoResourceFoundException")
    void handleResourceNotFound_shouldReturn404() {
        log.info("Executing test: handleResourceNotFound_shouldReturn404");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/v1/unknown", "Resource not found");

        ResponseEntity<ApiError> response = handler.handleResourceNotFound(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("ERR-0011", response.getBody().errorCode());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Resource not found", response.getBody().message());
    }

    @Test
    @DisplayName("handleAccessDenied - Returns 403 FORBIDDEN for Spring Security AccessDeniedException")
    void handleAccessDenied_shouldReturn403() {
        log.info("Executing test: handleAccessDenied_shouldReturn403");
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("ERR-0006", response.getBody().errorCode());
        assertEquals("Forbidden", response.getBody().error());
        assertEquals("Access denied: insufficient permissions", response.getBody().message());
    }

    @Test
    @DisplayName("handleUnexpectedException - Returns 500 INTERNAL SERVER ERROR for unexpected generic Exception")
    void handleUnexpectedException_shouldReturn500() {
        log.info("Executing test: handleUnexpectedException_shouldReturn500");
        Exception ex = new RuntimeException("Unexpected null pointer in service");

        ResponseEntity<ApiError> response = handler.handleUnexpectedException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("ERR-0001", response.getBody().errorCode());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    // =========================================================================
    // API ERROR RECORD TESTS
    // =========================================================================

    @Test
    @DisplayName("ApiError - 7-arg and 6-arg constructor overloads and accessors")
    void testApiErrorConstructors() {
        log.info("Executing test: testApiErrorConstructors");

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Map<String, String> errors = Map.of("configurationName", "Configuration name is required");

        // 1. 7-arg constructor with explicit errorCode
        ApiError err7 = new ApiError(
                now, 400, "Bad Request", "ERR-0008",
                "Validation failed", "/api/v1/platform-configurations", errors
        );
        assertEquals(now, err7.timestamp());
        assertEquals(400, err7.status());
        assertEquals("Bad Request", err7.error());
        assertEquals("ERR-0008", err7.errorCode());
        assertEquals("Validation failed", err7.message());
        assertEquals("/api/v1/platform-configurations", err7.path());
        assertThat(err7.validationErrors()).containsEntry("configurationName", "Configuration name is required");

        // 2. 6-arg constructor defaulting errorCode to "ERR-GENERIC"
        ApiError err6 = new ApiError(
                now, 404, "Not Found",
                "Entity not found", "/api/v1/platform-configurations/123", null
        );
        assertEquals("ERR-GENERIC", err6.errorCode());
        assertEquals(404, err6.status());
        assertThat(err6.validationErrors()).isNull();
    }

    // =========================================================================
    // CUSTOM DOMAIN EXCEPTIONS TESTS
    // =========================================================================

    @Test
    @DisplayName("Custom Exceptions - Hierarchy and detail message contracts")
    void testCustomDomainExceptions() {
        log.info("Executing test: testCustomDomainExceptions");

        ConfigurationActivationException ex1 = new ConfigurationActivationException("Activation rejected");
        assertThat(ex1).isInstanceOf(RuntimeException.class);
        assertEquals("Activation rejected", ex1.getMessage());

        ConfigurationImportException ex2 = new ConfigurationImportException("Import failure");
        assertThat(ex2).isInstanceOf(RuntimeException.class);
        assertEquals("Import failure", ex2.getMessage());

        ConfigurationRepositoryUnavailableException ex3 = new ConfigurationRepositoryUnavailableException("Database down");
        assertThat(ex3).isInstanceOf(RuntimeException.class);
        assertEquals("Database down", ex3.getMessage());

        DuplicateConfigurationNameException ex4 = new DuplicateConfigurationNameException("Duplicate key");
        assertThat(ex4).isInstanceOf(RuntimeException.class);
        assertEquals("Duplicate key", ex4.getMessage());

        InvalidConfigurationValueException ex5 = new InvalidConfigurationValueException("Invalid session value");
        assertThat(ex5).isInstanceOf(RuntimeException.class);
        assertEquals("Invalid session value", ex5.getMessage());

        PlatformConfigurationNotFoundException ex6 = new PlatformConfigurationNotFoundException("Config not found");
        assertThat(ex6).isInstanceOf(RuntimeException.class);
        assertEquals("Config not found", ex6.getMessage());

        UnauthorizedConfigurationAccessException ex7 = new UnauthorizedConfigurationAccessException("Super-admin only");
        assertThat(ex7).isInstanceOf(RuntimeException.class);
        assertEquals("Super-admin only", ex7.getMessage());
    }
}
