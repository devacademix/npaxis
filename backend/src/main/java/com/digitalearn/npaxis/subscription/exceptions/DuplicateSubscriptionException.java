package com.digitalearn.npaxis.subscription.exceptions;

/**
 * Thrown when a user already has an active subscription and attempts to create another.
 */
public class DuplicateSubscriptionException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Long userId;

    public DuplicateSubscriptionException(Long userId) {
        super("User already has an active subscription: " + userId);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}

