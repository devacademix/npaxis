package com.digitalearn.npaxis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Production environment variables validator.
 * Ensures all required configuration is present before application startup.
 * Fails fast to prevent runtime issues in production.
 *
 * This validator is only active in 'prod' profile.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod", matchIfMissing = false)
public class EnvironmentValidationConfig {

    public EnvironmentValidationConfig(Environment environment) {
        validateProductionConfig(environment);
    }

    /**
     * Validates that all required environment variables are set for production.
     * Fails fast if any critical configuration is missing.
     *
     * @param environment Spring Environment
     * @throws IllegalStateException if any required env var is missing
     */
    private void validateProductionConfig(Environment environment) {
        List<String> missingVars = new ArrayList<>();

        // Database configuration
        validateVariable(environment, "DB_URL", missingVars);
        validateVariable(environment, "DB_USERNAME", missingVars);
        validateVariable(environment, "DB_PASSWORD", missingVars);

        // Security configuration
        validateVariable(environment, "JWT_SECRET", missingVars);

        // Email configuration
        validateVariable(environment, "MAIL_HOST", missingVars);
        validateVariable(environment, "MAIL_PORT", missingVars);
        validateVariable(environment, "MAIL_USERNAME", missingVars);
        validateVariable(environment, "MAIL_PASSWORD", missingVars);
        validateVariable(environment, "EMAIL_FROM_ADDRESS", missingVars);
        validateVariable(environment, "EMAIL_FROM_NAME", missingVars);

        // DigitalOcean Spaces configuration
        validateVariable(environment, "DO_SPACES_ACCESS_KEY", missingVars);
        validateVariable(environment, "DO_SPACES_SECRET_KEY", missingVars);
        validateVariable(environment, "DO_SPACES_BUCKET", missingVars);
        validateVariable(environment, "DO_SPACES_CDN_ENDPOINT", missingVars);

        // Stripe configuration (if payment is enabled)
        validateVariable(environment, "STRIPE_SECRET_KEY", missingVars);
        validateVariable(environment, "STRIPE_PUBLISHABLE_KEY", missingVars);
        validateVariable(environment, "STRIPE_WEBHOOK_SECRET", missingVars);
        validateVariable(environment, "STRIPE_SUCCESS_URL", missingVars);
        validateVariable(environment, "STRIPE_CANCEL_URL", missingVars);
        validateVariable(environment, "STRIPE_PORTAL_RETURN_URL", missingVars);

        // CORS configuration
        validateVariable(environment, "NPAXIS_CORS_ALLOWED_ORIGINS", missingVars);

        if (!missingVars.isEmpty()) {
            String message = String.format(
                    "Production deployment failed! Missing %d required environment variables:\n%s\n" +
                    "Please ensure all environment variables are set before deploying to production.",
                    missingVars.size(),
                    String.join("\n- ", missingVars)
            );
            log.error(message);
            throw new IllegalStateException(message);
        }

        log.info("✓ All {} required production environment variables are configured", 12 + 6 + 6);
    }

    /**
     * Checks if an environment variable is set and not empty.
     *
     * @param environment Environment to check
     * @param varName     Variable name
     * @param missingVars List to collect missing variables
     */
    private void validateVariable(Environment environment, String varName, List<String> missingVars) {
        String value = environment.getProperty(varName);
        if (value == null || value.trim().isEmpty()) {
            missingVars.add(varName);
            log.debug("Missing environment variable: {}", varName);
        }
    }
}
