package com.example.microservice.organizations.company.exception;

public final class CompanyConflictException extends RuntimeException {
    public CompanyConflictException(String message) {
        super(message);
    }
}
