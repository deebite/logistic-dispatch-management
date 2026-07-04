package com.logistic.dispatch.config;

import com.logistic.dispatch.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Authentication
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/grt-report/create").permitAll()
                        .requestMatchers("/api/auth/logout").authenticated()

                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Employee Management
                        .requestMatchers("/api/employee/**")
                        .hasAnyRole("ADMIN", "SUPERVISOR")

                        // ---------------- Products ----------------
                        // Create
                        .requestMatchers(HttpMethod.POST, "/api/products/create")
                        .hasAnyRole("ADMIN", "SUPERVISOR")

                        // Read
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyRole("ADMIN", "SUPERVISOR", "OPERATOR")

                        // Update
                        .requestMatchers(HttpMethod.PUT, "/api/products/update/**")
                        .hasAnyRole("ADMIN", "SUPERVISOR")

                        // Change Status
                        .requestMatchers(HttpMethod.PATCH, "/api/products/*/status")
                        .hasAnyRole("ADMIN", "SUPERVISOR")

                        // Update Image
                        .requestMatchers(HttpMethod.PATCH, "/api/products/*/image")
                        .hasAnyRole("ADMIN", "SUPERVISOR")

                        // Delete
                        .requestMatchers(HttpMethod.DELETE, "/api/products/delete/**")
                        .hasAnyRole("ADMIN", "SUPERVISOR")

                        // ---------------- Batch ----------------
                        .requestMatchers("/api/batch/**")
                        .hasAnyRole("ADMIN", "SUPERVISOR", "OPERATOR")

                        // ---------------- Pallet ----------------
                        .requestMatchers("/api/pallet/**")
                        .hasAnyRole("ADMIN", "SUPERVISOR", "OPERATOR")

                        .requestMatchers("/api/report/**")
                        .hasAnyRole("ADMIN", "SUPERVISOR")

                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}