package com.digitalearn.npaxis.config;

import com.digitalearn.npaxis.user.User;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementation of {@link AuditorAware} to capture the current user's ID for auditing purposes.
 * <p>
 * This class is used by Spring Data JPA to automatically populate the `@CreatedBy` and `@LastModifiedBy` fields
 * in entities. It retrieves the currently authenticated user's ID from the Spring Security context.
 * </p>
 * <p>
 * Author: Molu Tyagi
 *
 * @version 1.0
 */
@Slf4j
public class ApplicationAuditAware implements AuditorAware<Long> {

    /**
     * Retrieves the user ID of the currently authenticated user for auditing purposes.
     * <p>
     * This method checks the Spring Security context for the current authentication. If the user is authenticated,
     * it extracts the user ID from the `User` object stored in the principal. If no user is authenticated or the
     * authentication is anonymous, it returns an empty `Optional`.
     * </p>
     *
     * @return An {@link Optional} containing the user ID if available; otherwise, an empty `Optional`.
     */
    @Override
    @Nonnull
    public Optional<Long> getCurrentAuditor() {
        log.debug("Accessing current auditor for audit logging...");

        // Retrieve the current authentication from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if the authentication is null, not authenticated, or an anonymous token
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            log.debug("No authenticated user found, returning empty auditor.");
            return Optional.empty();
        }

        // Extract the principal object from the authentication
        Object principal = authentication.getPrincipal();

        // Check if the principal is an instance of the User entity and extract the user ID
        if (principal instanceof User user) {
            log.debug("Authenticated user found for auditing: {} with userId={}", user.getDisplayName(), user.getUserId());
            return Optional.ofNullable(user.getUserId());
        }

        // Log a warning if the principal is of an unexpected type
        assert principal != null;
        log.warn("Unexpected principal type found: {}", principal.getClass().getName());
        return Optional.empty();
    }
}
