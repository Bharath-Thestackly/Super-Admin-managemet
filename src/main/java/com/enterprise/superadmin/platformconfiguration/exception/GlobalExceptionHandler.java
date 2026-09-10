package com.enterprise.superadmin.platformconfiguration.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global centralized REST exception handler intercepting framework and business exceptions.
 *
 * <p>Translates all errors into standardized RFC 7807 / FRS-compliant {@link ApiError} payloads
 * containing HTTP status code, textual phrase, FRS error code (e.g., ERR-0007, ERR-0008, ERR-0009,
 * ERR-0011, ERR-0012), sanitized error message, request path, and field-level validation maps.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles resource not found scenarios (FRS ERR-0011).
     *
     * @param exception the thrown not-found exception
     * @param request   the executing servlet request
     * @return 404 NOT_FOUND response with ERR-0011 code
     */
    @ExceptionHandler(PlatformConfigurationNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(PlatformConfigurationNotFoundException exception,
                                                   HttpServletRequest request) {
        log.warn("Resource not found: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "ERR-0011", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles duplicate configuration name conflict (ERR-0007, BR-0012).
     */
    @ExceptionHandler(DuplicateConfigurationNameException.class)
    public ResponseEntity<ApiError> handleDuplicateConfiguration(DuplicateConfigurationNameException exception,
                                                                 HttpServletRequest request) {
        log.warn("Conflict detected: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "ERR-0007", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles database unique constraint violations arising from concurrent requests (ERR-0007).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException exception,
                                                                HttpServletRequest request) {
        log.warn("Database constraint violation on {} {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        String msg = "A configuration with this name already exists or violates database integrity constraints.";
        return buildResponse(HttpStatus.CONFLICT, "ERR-0007", msg, request.getRequestURI(), Map.of());
    }

    /**
     * Handles invalid configuration values (ERR-0008, VAL-0008, VAL-0010).
     */
    @ExceptionHandler(InvalidConfigurationValueException.class)
    public ResponseEntity<ApiError> handleInvalidConfigurationValue(InvalidConfigurationValueException exception,
                                                                    HttpServletRequest request) {
        log.warn("Invalid configuration value: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0008", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles pre-activation validation failure (ERR-0009, VAL-0012, BR-0020).
     */
    @ExceptionHandler(ConfigurationActivationException.class)
    public ResponseEntity<ApiError> handleConfigurationActivation(ConfigurationActivationException exception,
                                                                  HttpServletRequest request) {
        log.warn("Configuration activation failure: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0009", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles configuration import failure (ERR-0010).
     */
    @ExceptionHandler(ConfigurationImportException.class)
    public ResponseEntity<ApiError> handleConfigurationImport(ConfigurationImportException exception,
                                                              HttpServletRequest request) {
        log.warn("Configuration import failure: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0010", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles security authorization failure (ERR-0006).
     */
    @ExceptionHandler(UnauthorizedConfigurationAccessException.class)
    public ResponseEntity<ApiError> handleUnauthorizedAccess(UnauthorizedConfigurationAccessException exception,
                                                             HttpServletRequest request) {
        log.warn("Unauthorized access attempt: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "ERR-0006", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles database/infrastructure connectivity failures (ERR-0012).
     */
    @ExceptionHandler(ConfigurationRepositoryUnavailableException.class)
    public ResponseEntity<ApiError> handleRepositoryUnavailable(ConfigurationRepositoryUnavailableException exception,
                                                                HttpServletRequest request) {
        log.error("Configuration repository unavailable: {} {}", request.getMethod(), request.getRequestURI(),
                exception);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "ERR-0012", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles request body bean validation failures (ERR-0008).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
                                                     HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation error on request: {} {} - errors: {}", request.getMethod(), request.getRequestURI(),
                validationErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0008", "Request validation failed", request.getRequestURI(),
                validationErrors);
    }

    /**
     * Handles unreadable or malformed JSON payloads (ERR-0008).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest(HttpMessageNotReadableException exception,
                                                            HttpServletRequest request) {
        String message = "Invalid request body.";
        if (exception.getCause() instanceof InvalidFormatException ife && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {
            message = "Invalid value for " + resolveFieldName(ife) + ".";
        }

        log.warn("Malformed HTTP request body: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0008", message, request.getRequestURI(), Map.of());
    }

    /**
     * Handles unmapped endpoints (ERR-0011).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(NoResourceFoundException exception,
                                                           HttpServletRequest request) {
        log.warn("Unmapped endpoint requested: {} {}", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, "ERR-0011", "Resource not found", request.getRequestURI(), Map.of());
    }

    /**
     * Handles Spring Security access denied (ERR-0006).
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(org.springframework.security.access.AccessDeniedException exception,
                                                       HttpServletRequest request) {
        log.warn("Access denied: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "ERR-0006", "Access denied: insufficient permissions", request.getRequestURI(), Map.of());
    }

    /**
     * Fallback handler for unexpected internal server errors (ERR-0001).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled internal server error: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage(), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERR-0001", "An unexpected error occurred", request.getRequestURI(),
                Map.of());
    }

    private String resolveFieldName(InvalidFormatException exception) {
        if (exception.getPath() != null && !exception.getPath().isEmpty()
                && exception.getPath().get(0).getPropertyName() != null) {
            return exception.getPath().get(0).getPropertyName();
        }
        return "request field";
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String errorCode, String message, String path,
                                                   Map<String, String> validationErrors) {
        ApiError error = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), errorCode, message, path,
                validationErrors);
        return ResponseEntity.status(status).body(error);
    }
}