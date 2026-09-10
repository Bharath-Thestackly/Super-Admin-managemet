package com.enterprise.platformhealthservice.integration;

public interface ServiceHealthClient {

    ServiceHealthResult getHealth(String serviceName);
}