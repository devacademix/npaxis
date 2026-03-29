package com.digitalearn.npaxis.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class for setting up HTTP security, authentication, and authorization.
 * This class configures the security filter chain, including CORS, CSRF, session management,
 * and URL access rules based on user roles.
 */
@Configuration
@Slf4j
public class SecurityConfig {

    /**
     * Configures the security filter chain for the application.
     * Open mode: all endpoints are publicly accessible (dev-only).
     *
     * @param httpSecurity the HttpSecurity object for configuring security
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.warn("Configuring SecurityFilterChain in OPEN mode (authentication disabled).");
        httpSecurity

                // === HEADERS ===
                .headers(headers -> headers.frameOptions(frameOptionsConfig -> {
                    frameOptionsConfig.disable();
                    frameOptionsConfig.sameOrigin();
                }))

                // === CORS and CSRF ===
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                // === URL Access Rules===
                .authorizeHttpRequests(
                        req -> req.anyRequest().permitAll()
                )

                // === Session Management ===
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        log.debug("SecurityFilterChain OPEN mode configuration completed.");

        return httpSecurity.build();
    }
}
