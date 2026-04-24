package com.digitalearn.npaxis.subscription.exceptions;

/**
 * Thrown when a subscription operation is attempted on a subscription in an invalid state.
 * For example: attempting to cancel an already canceled subscription.
 */
public class InvalidSubscriptionStateException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String reason;

    public InvalidSubscriptionStateException(String reason) {
        super("Invalid subscription state: " + reason);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}

