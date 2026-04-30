package com.digitalearn.npaxis.subscription.exceptions;

/**
 * Thrown when a subscription cannot be found for a given user.
 */
public class SubscriptionNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Long userId;

    public SubscriptionNotFoundException(Long userId) {
        super("Subscription not found for user: " + userId);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}

