package com.digitalearn.npaxis.analytics;

import com.digitalearn.npaxis.user.User;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for analytics-related operations.
 * <p>
 * Provides helper methods for:
 * - Extracting current user information
 * - Building metadata maps
 * - Manual event tracking (when needed)
 * <p>
 * USAGE:
 * - Use for manual tracking when @TrackEvent annotation is insufficient
 * - Use in services for complex tracking conditions
 * <p>
 * BEST PRACTICES:
 * - Prefer @TrackEvent annotation over manual tracking
 * - Use these utilities only for edge cases and complex scenarios
 *
 * @author Backend Team
 * @version 1.0
 */
@UtilityClass
@Slf4j
public class AnalyticsUtils {

    /**
     * Extracts the current authenticated user from Spring Security context.
     *
     * @return Optional containing the User if authenticated, empty otherwise
     */
    public static Optional<User> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return Optional.of(user);
            }

            return Optional.empty();

        } catch (Exception e) {
            log.warn("Error extracting current user", e);
            return Optional.empty();
        }
    }

    /**
     * Extracts the ID of the current authenticated user.
     *
     * @return the userId if authenticated, null otherwise
     */
    public static Long getCurrentUserId() {
        return getCurrentUser()
                .map(User::getUserId)
                .orElse(null);
    }

    /**
     * Extracts the email of the current authenticated user.
     *
     * @return the user email if authenticated, null otherwise
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser()
                .map(User::getEmail)
                .orElse(null);
    }

    /**
     * Checks if a user is currently authenticated.
     *
     * @return true if authenticated and not anonymous
     */
    public static boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    /**
     * Creates a metadata map with standard key-value pairs.
     * <p>
     * This is useful for building metadata when using @TrackEvent or manual tracking.
     * <p>
     * EXAMPLE:
     * <pre>
     * Map<String, Object> metadata = AnalyticsUtils.createMetadata()
     *     .add("action", "viewed")
     *     .add("duration_ms", 5000)
     *     .build();
     * </pre>
     *
     * @return a new MetadataBuilder instance
     */
    public static MetadataBuilder createMetadata() {
        return new MetadataBuilder();
    }

    /**
     * Creates a standard metadata entry for search events.
     *
     * @param query       the search query
     * @param filters     applied filters (as string)
     * @param resultCount number of results
     * @return metadata map
     */
    public static Map<String, Object> createSearchMetadata(
            String query,
            String filters,
            int resultCount) {

        return createMetadata()
                .add("query", query)
                .addIfNotNull("filters", filters)
                .add("resultCount", resultCount)
                .build();
    }

    /**
     * Creates a standard metadata entry for contact events.
     *
     * @param contactType the type of contact (phone, email, etc.)
     * @param duration    duration of interaction in milliseconds
     * @return metadata map
     */
    public static Map<String, Object> createContactMetadata(
            String contactType,
            long duration) {

        return createMetadata()
                .add("contactType", contactType)
                .add("duration_ms", duration)
                .build();
    }

    /**
     * Creates a standard metadata entry for subscription events.
     *
     * @param plan          the subscription plan name
     * @param amount        the amount in minor units (cents)
     * @param billingPeriod the billing period (monthly, yearly, etc.)
     * @return metadata map
     */
    public static Map<String, Object> createSubscriptionMetadata(
            String plan,
            long amount,
            String billingPeriod) {

        return createMetadata()
                .add("plan", plan)
                .add("amount_minor_units", amount)
                .add("billing_period", billingPeriod)
                .build();
    }

    /**
     * Builder class for constructing metadata maps fluently.
     * <p>
     * This makes it easier to build metadata in complex scenarios.
     * <p>
     * EXAMPLE:
     * <pre>
     * AnalyticsUtils.createMetadata()
     *     .add("specialization", "Cardiology")
     *     .add("experience_years", 15)
     *     .add("location", "New York")
     *     .build();
     * </pre>
     */
    public static class MetadataBuilder {
        private final Map<String, Object> metadata = new HashMap<>();

        /**
         * Adds a key-value pair to the metadata.
         *
         * @param key   the key
         * @param value the value (can be null)
         * @return this builder for chaining
         */
        public MetadataBuilder add(String key, Object value) {
            if (key != null && !key.isBlank()) {
                metadata.put(key, value);
            }
            return this;
        }

        /**
         * Adds a key-value pair only if the value is non-null.
         *
         * @param key   the key
         * @param value the value (skipped if null)
         * @return this builder for chaining
         */
        public MetadataBuilder addIfNotNull(String key, Object value) {
            if (value != null) {
                add(key, value);
            }
            return this;
        }

        /**
         * Adds all entries from another map.
         *
         * @param entries the entries to add
         * @return this builder for chaining
         */
        public MetadataBuilder addAll(Map<String, Object> entries) {
            if (entries != null) {
                metadata.putAll(entries);
            }
            return this;
        }

        /**
         * Builds the metadata map.
         *
         * @return an unmodifiable copy of the metadata
         */
        public Map<String, Object> build() {
            return new HashMap<>(metadata);
        }
    }

}

