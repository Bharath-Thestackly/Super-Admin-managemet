package com.enterprise.platform.admin.superadmindashboard.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final RoleHeaderAuthenticationFilter roleHeaderAuthenticationFilter;

    public SecurityConfig(RoleHeaderAuthenticationFilter roleHeaderAuthenticationFilter) {
        this.roleHeaderAuthenticationFilter = roleHeaderAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/dashboard/**").hasAuthority("SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(roleHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<RoleHeaderAuthenticationFilter> disableAutoRegistration(
            RoleHeaderAuthenticationFilter filter) {
        FilterRegistrationBean<RoleHeaderAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}