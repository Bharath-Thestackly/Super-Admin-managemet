package com.enterprise.superadmin.feature_management_service.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig  {

    @Bean
    public OpenAPI featureManagementOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "Enterprise Feature Management API"
                                )
                                .version("1.0.0")
                                .description(
                                        "Team Wolf Feature Management Service"
                                )
                );
    }
}
