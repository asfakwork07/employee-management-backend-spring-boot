package com.asfak.employee_management_backend.config;

import com.asfak.employee_management_backend.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // ================================
                        // PUBLIC
                        // ================================

                        .requestMatchers(
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        // ================================
                        // EMAIL
                        // ================================

                        .requestMatchers(
                                "/api/email/**"
                        )
                        .hasRole("ADMIN")

                        // ================================
                        // AI PERFORMANCE - EMPLOYEE SELF
                        // ================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/ai/chat"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "EMPLOYEE"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/ai/performance/me"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "EMPLOYEE"
                        )

                        // ================================
                        // AI - ADMIN ACCESS
                        // ================================

                        .requestMatchers(
                                "/api/ai/**"
                        )
                        .hasRole("ADMIN")

                        // ================================
                        // EMPLOYEE PROFILE
                        // ================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/employees/me"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "EMPLOYEE"
                        )

                        // ================================
                        // HOLIDAYS
                        // ================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/holidays/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/holidays/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/holidays/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/holidays/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "EMPLOYEE"
                        )

                        // ================================
                        // SALARY ADMIN
                        // ================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/salary/generate"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/salary"
                        )
                        .hasRole("ADMIN")

                        // ================================
                        // ATTENDANCE ADMIN
                        // ================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/attendance"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/attendance/today"
                        )
                        .hasRole("ADMIN")

                        // ================================
                        // LEAVES ADMIN
                        // ================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/leaves"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/leaves/*/approve",
                                "/api/leaves/*/reject"
                        )
                        .hasRole("ADMIN")

                        // ================================
                        // ADMIN ONLY MODULES
                        // ================================

                        .requestMatchers(
                                "/api/employees/**",
                                "/api/roles/**",
                                "/api/users/**",
                                "/api/settings/**"
                        )
                        .hasRole("ADMIN")

                        // ================================
                        // ADMIN + EMPLOYEE MODULES
                        // ================================

                        .requestMatchers(
                                "/api/dashboard/**",
                                "/api/attendance/**",
                                "/api/leaves/**",
                                "/api/leave-types/**",
                                "/api/salary/**",
                                "/api/notifications/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "EMPLOYEE"
                        )

                        // ================================
                        // CHANGE PASSWORD
                        // ================================

                        .requestMatchers(
                                "/api/auth/change-password"
                        )
                        .authenticated()

                        // ================================
                        // FALLBACK
                        // ================================

                        .anyRequest()
                        .authenticated()
                )

                .authenticationProvider(
                        authenticationProvider
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}