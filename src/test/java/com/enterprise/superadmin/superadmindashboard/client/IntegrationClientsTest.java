package com.enterprise.superadmin.superadmindashboard.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IntegrationClientsTest {

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PlatformHealthClient healthClient;
    private TenantClient tenantClient;
    private UserClient userClient;
    private SubscriptionClient subscriptionClient;

    @BeforeEach
    void setUp() {
        healthClient = new PlatformHealthClient(restTemplate, objectMapper, "http://localhost:8081/actuator/health");
        tenantClient = new TenantClient(restTemplate, objectMapper, "http://tenant-service", "http://localhost:8082");
        userClient = new UserClient(restTemplate, objectMapper, "http://user-service", "http://localhost:8083");
        subscriptionClient = new SubscriptionClient(restTemplate, objectMapper, "http://subscription-service", "http://localhost:8084");
    }

    @Test
    @DisplayName("HealthClient parses UP status and catches timeouts with UNKNOWN fallback")
    void testHealthClient() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{\"status\":\"UP\"}");
        assertEquals("UP", healthClient.getPlatformHealth());

        // Timeout simulation
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));
        assertEquals("UNKNOWN", healthClient.getPlatformHealth());
    }

    @Test
    @DisplayName("TenantClient parses count and returns Optional.empty() on connection failure")
    void testTenantClient() {
        when(restTemplate.getForObject(eq("http://localhost:8082/api/v1/tenants/count"), eq(String.class)))
                .thenReturn("{\"count\":42}");
        assertEquals(Optional.of(42L), tenantClient.getTotalTenants());

        when(restTemplate.getForObject(eq("http://localhost:8082/api/v1/tenants/count"), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connect timed out"));
        assertTrue(tenantClient.getTotalTenants().isEmpty());
    }

    @Test
    @DisplayName("UserClient parses count and returns Optional.empty() on connection failure")
    void testUserClient() {
        when(restTemplate.getForObject(eq("http://localhost:8083/api/v1/users/count"), eq(String.class)))
                .thenReturn("{\"count\":350}");
        assertEquals(Optional.of(350L), userClient.getTotalUsers());

        when(restTemplate.getForObject(eq("http://localhost:8083/api/v1/users/count"), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));
        assertTrue(userClient.getTotalUsers().isEmpty());
    }

    @Test
    @DisplayName("SubscriptionClient parses count and returns Optional.empty() on connection failure")
    void testSubscriptionClient() {
        when(restTemplate.getForObject(eq("http://localhost:8084/api/v1/subscriptions/count"), eq(String.class)))
                .thenReturn("{\"count\":29}");
        assertEquals(Optional.of(29L), subscriptionClient.getActiveSubscriptions());

        when(restTemplate.getForObject(eq("http://localhost:8084/api/v1/subscriptions/count"), eq(String.class)))
                .thenThrow(new ResourceAccessException("Host unreachable"));
        assertTrue(subscriptionClient.getActiveSubscriptions().isEmpty());
    }
}