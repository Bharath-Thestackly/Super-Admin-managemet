package com.enterprise.platform.admin.superadmindashboard.client;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Component public class PlatformHealthClient {
    private final RestTemplate restTemplate;

    public PlatformHealthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public String getPlatformHealth() {
        String url = "http://localhost:8081/actuator/health";
        return restTemplate.getForObject(url, String.class);
    }
}