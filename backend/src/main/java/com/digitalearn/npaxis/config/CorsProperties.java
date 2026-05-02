package com.digitalearn.npaxis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for CORS (Cross-Origin Resource Sharing).
 * Allows dynamic configuration of allowed origins via environment variables.
 *
 * Usage in application.yml:
 * npaxis:
 *   cors:
 *     allowed-origins: https://npaxis.com,https://www.npaxis.com
 *
 * Or via environment variable:
 * NPAXIS_CORS_ALLOWED_ORIGINS=https://npaxis.com,https://www.npaxis.com
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "npaxis.cors")
public class CorsProperties {
    /**
     * Comma-separated list of allowed origins for CORS requests.
     * Examples:
     * - Development: http://localhost:3000,http://localhost:5173
     * - Production: https://npaxis.com,https://www.npaxis.com
     *
     * Default: http://localhost:3000 (development only)
     */
    private String allowedOrigins = "http://localhost:3000,http://localhost:5173,http://localhost:5174";

    /**
     * Parse allowed origins into a list.
     *
     * @return List of allowed origin strings
     */
    public List<String> getAllowedOriginsList() {
        List<String> origins = new ArrayList<>();
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            String[] originArray = allowedOrigins.split(",");
            for (String origin : originArray) {
                String trimmedOrigin = origin.trim();
                if (!trimmedOrigin.isEmpty()) {
                    origins.add(trimmedOrigin);
                }
            }
        }
        return origins;
    }
}

