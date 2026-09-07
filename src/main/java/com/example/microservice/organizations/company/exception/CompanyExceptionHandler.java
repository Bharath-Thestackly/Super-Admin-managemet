package com.example.microservice.organizations.company.exception;

import com.example.microservice.common.exception.ErrorResponse;
import com.example.microservice.common.exception.ResourceNotFoundException;
import com.example.microservice.organizations.company.controller.CompanyController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = CompanyController.class)
public class CompanyExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CompanyExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException exception,
                                                      HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String name = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            fields.putIfAbsent(name, error.getDefaultMessage());
        });
        return response(HttpStatus.BAD_REQUEST, "Validation Failed", "Input validation error", fields, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, ConstraintViolationException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception exception, HttpServletRequest request) {
        String message;
        if (exception instanceof MissingServletRequestParameterException missing) {
            message = "Required parameter is missing: " + missing.getParameterName();
        } else if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            message = "Invalid value for " + mismatch.getName();
        } else if (exception instanceof IllegalArgumentException) {
            message = exception.getMessage();
        } else {
            message = "Malformed or invalid request";
        }
        return response(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), message, null, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException exception,
                                                   HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(), null, request);
    }

    @ExceptionHandler({CompanyConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> conflict(Exception exception, HttpServletRequest request) {
        String message = exception instanceof CompanyConflictException
                ? exception.getMessage() : "Company data conflicts with an existing record";
        return response(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), message, null, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> forbidden(HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(),
                "You do not have permission to access this resource", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected Company API failure", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected internal error occurred", null, request);
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String error, String message,
                                                    Map<String, String> fields, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .success(false)
                .status(status.value())
                .error(error)
                .message(message)
                .validationErrors(fields)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
