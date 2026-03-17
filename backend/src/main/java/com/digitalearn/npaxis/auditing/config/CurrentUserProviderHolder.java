package com.digitalearn.npaxis.auditing.config;

import lombok.Setter;

import java.util.Optional;

/**
 * Static accessor for the CurrentUserProvider.
 * <p>
 * The actual implementation should be set by the application context
 * in each service at runtime.
 */
public class CurrentUserProviderHolder {

    @Setter
    private static CustomAuditorAware<Long> auditor;

    public static Optional<Long> getCurrentUserId() {
        if (auditor == null) {
            return Optional.empty();
        }
        return auditor.getCurrentAuditor();
    }
}
