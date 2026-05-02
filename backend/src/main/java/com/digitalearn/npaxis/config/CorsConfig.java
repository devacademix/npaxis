package com.digitalearn.npaxis.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for CORS (Cross-Origin Resource Sharing) support.
 * This class enables CORS for all endpoints using configurable origins from environment variables.
 *
 * Configuration can be set via:
 * 1. application.yml: npaxis.cors.allowed-origins
 * 2. Environment variable: NPAXIS_CORS_ALLOWED_ORIGINS
 *
 * Examples:
 * - Development: http://localhost:3000,http://localhost:5173,http://localhost:5174
 * - Production: https://npaxis.com,https://www.npaxis.com
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class CorsConfig {

    private final CorsProperties corsProperties;

    /**
     * Configures CORS for all endpoints using dynamic origins from configuration.
     *
     * @return a CorsFilter bean configured with allowed origins
     */
    @Bean
    CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        // Get allowed origins from configuration
        List<String> allowedOrigins = corsProperties.getAllowedOriginsList();
        log.info("Configuring CORS with allowed origins: {}", allowedOrigins);

        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.COOKIE,
                HttpHeaders.SET_COOKIE
        ));

        config.setExposedHeaders(List.of(HttpHeaders.SET_COOKIE));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
