package com.digitalearn.npaxis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for CORS (Cross-Origin Resource Sharing) support. This class enables CORS for all endpoints.
 */
@Configuration
public class CorsConfig {

    /**
     * Configures CORS for all endpoints.
     *
     * @return a CorsFilter bean.
     */
    @Bean
    CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:5174"
        ));
        config.setAllowedHeaders(Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION, HttpHeaders.COOKIE, HttpHeaders.SET_COOKIE));

        config.setExposedHeaders(List.of(HttpHeaders.SET_COOKIE));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
