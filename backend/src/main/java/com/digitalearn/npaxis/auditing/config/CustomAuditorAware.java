package com.digitalearn.npaxis.auditing.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Abstraction to retrieve the current authenticated user's ID.
 * <p>
 * Implement this interface in each microservice according to its security setup.
 * (e.g., from Spring Security, JWT claims, thread-local, etc.)
 */
public interface CustomAuditorAware<T> extends AuditorAware<T> {

    /**
     * @return the ID of the currently authenticated user, or null/system ID if none.
     */
    Optional<T> getCurrentAuditor();
}
