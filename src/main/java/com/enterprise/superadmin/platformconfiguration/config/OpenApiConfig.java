package com.enterprise.superadmin.platformconfiguration.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 configuration using SpringDoc for interactive API documentation and testing.
 *
 * <p>Exposes all Platform Configuration REST endpoints through the Swagger UI (available at
 * {@code /swagger-ui.html} and {@code /v3/api-docs}). Configures global security schemes allowing
 * testing with custom gateway role headers when enabled.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String ROLE_HEADER_SCHEME = "GatewayRoleAuth";

    /**
     * Configures the global {@link OpenAPI} metadata and security scheme requirements.
     *
     * @return initialized {@link OpenAPI} configuration model
     */
    @Bean
    public OpenAPI platformConfigurationOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Super Admin Management Service API")
                .version("v1")
                .description("Super Admin Platform Configuration APIs with Gateway Role Authentication"))
            // Applies the header globally across all Swagger endpoints
            .addSecurityItem(new SecurityRequirement().addList(ROLE_HEADER_SCHEME))
            .components(new Components()
                .addSecuritySchemes(ROLE_HEADER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-User-Role")
                        .description("Enter your simulation role: SUPER_ADMIN, ADMIN, or VIEWER")
                )
            );
    }
}