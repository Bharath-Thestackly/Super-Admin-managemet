package com.enterprise.superadmin.superadmindashboard.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class DashboardContractClient {

    private static final Logger log = LoggerFactory.getLogger(DashboardContractClient.class);

    private final RestTemplate restTemplate;
    private final String globalDashboardUrl;

    public DashboardContractClient(
            RestTemplate restTemplate,
            @Value("${services.global-dashboard-service.url:http://localhost:8085/api/v1/global-dashboard}") String globalDashboardUrl) {
        this.restTemplate = restTemplate;
        this.globalDashboardUrl = globalDashboardUrl;
    }

    public Optional<GlobalDashboardContractResponse> getGlobalDashboardContract() {
        try {
            GlobalDashboardContractResponse response =
                    restTemplate.getForObject(globalDashboardUrl, GlobalDashboardContractResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientException e) {
            log.warn("Global dashboard service unreachable at {}: {}. Will use graceful fallback.",
                    globalDashboardUrl, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to parse global dashboard contract response: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
