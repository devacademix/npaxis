package com.digitalearn.npaxis.security.jwt;

import com.digitalearn.npaxis.exceptions.CustomAccessDeniedHandler;
import com.digitalearn.npaxis.role.RoleName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for setting up HTTP security, authentication, and authorization.
 * This class configures the security filter chain, including CORS, CSRF, session management,
 * and URL access rules based on user roles.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    /**
     * URL patterns for different access levels.
     * - USER_ONLY_URLS: URLs accessible only by users with the ADMIN role.
     * - ADMINISTRATION_ONLY_URLS: URLs accessible only by users with the SUPER_ADMIN role.
     * - PUBLIC_URLS: URLs are accessible to all users without authentication.
     */
    private static final String[] ADMINISTRATION_ONLY_URLS = {"/administration/**"};
    private static final String[] PROTECTED_AUTH_URLS = {"/api/v1/auth/logout"};
    private static final String[] USER_ONLY_URLS = {"/users/**"};
    private static final String[] PRECEPTOR_ONLY_URLS = {"/preceptors/**"};
    private static final String[] STUDENT_ONLY_URLS = {"/students/**"};
    private static final String[] PUBLIC_URLS = {"/auth/**", "/h2-console/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**"};
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Configures the security filter chain for the application.
     * This method sets up CORS, CSRF, session management, and URL access rules.
     *
     * @param httpSecurity the HttpSecurity object for configuring security
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.debug("Configuring SecurityFilterChain...");
        httpSecurity

                // === HEADERS ===
                .headers(headers -> headers.frameOptions(frameOptionsConfig -> {
                    frameOptionsConfig.disable();
                    frameOptionsConfig.sameOrigin();
                }))

                // === CORS and CSRF ===
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                // === URL Auth Rules===
                .authorizeHttpRequests(
                        req -> req.requestMatchers(PUBLIC_URLS).permitAll()
                                .requestMatchers(PROTECTED_AUTH_URLS).authenticated()
                                .requestMatchers(ADMINISTRATION_ONLY_URLS).hasRole(RoleName.ROLE_ADMIN.getRoleName())
                                .requestMatchers(USER_ONLY_URLS).hasAnyRole(RoleName.ROLE_PRECEPTOR.getRoleName(), RoleName.ROLE_STUDENT.getRoleName())
                                .requestMatchers(PRECEPTOR_ONLY_URLS).hasRole(RoleName.ROLE_PRECEPTOR.getRoleName())
                                .requestMatchers(STUDENT_ONLY_URLS).hasRole(RoleName.ROLE_STUDENT.getRoleName())

                                .anyRequest().authenticated()
                )

                // === Exception Handling ===
                .exceptionHandling(
                        exceptions -> exceptions.accessDeniedHandler(this.customAccessDeniedHandler)
                )

                // === Session Management ===
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // === Auth Provider and Filters ===
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        log.debug("SecurityFilterChain configuration completed.");

        return httpSecurity.build();
    }
}
