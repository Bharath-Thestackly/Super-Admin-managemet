package com.enterprise.superadmin.platform_settings_service.config;

import com.enterprise.superadmin.platform_settings_service.security.JwtAuthoritiesConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

// Security configuration class for the Platform Settings Service.
// It sets up security rules for HTTP requests, including authentication and
// authorization requirements, and configures JWT-based OAuth2 resource server settings.
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Permit access to Swagger UI and API documentation
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        // As of now, permit all requests to the platform settings API endpoints.
                        // This can be modified later to enforce authentication and authorization as needed.
                        .requestMatchers(
                                "/api/v1/platform-settings/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                // Configure the session management to be stateless, meaning that the server
                // will not maintain any session information between requests.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure the application as an OAuth2 resource server that uses JWT for authentication
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter());

        return converter;
    }

}