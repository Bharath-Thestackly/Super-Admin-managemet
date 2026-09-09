package com.enterprise.platformhealthservice.integration;

public record ServiceHealthResult(
        String serviceName,
        String status,
        String message
) {
}