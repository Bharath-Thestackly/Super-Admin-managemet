package com.enterprise.superadmin.superadmindashboard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI superAdminDashboardOpenAPI() {
        final String securitySchemeName = "basicAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("One Enterprise Platform - Super Admin Dashboard API")
                        .description("Sprint 1 Platform Administration APIs for Super Admin Dashboard aggregation, metrics, summary, and statistics.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Sai Bhargavi - Super Admin Dashboard Team")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));
    }
}