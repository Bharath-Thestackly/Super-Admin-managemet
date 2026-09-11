package com.enterprise.superadmin.platform_branding_service.config;

import com.enterprise.superadmin.platform_branding_service.integration.audit.AuditLogClient;
import com.enterprise.superadmin.platform_branding_service.integration.audit.LocalAuditLogClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditLogConfig {

    @Bean
    public AuditLogClient auditLogClient() {
        return new LocalAuditLogClient();
    }
}