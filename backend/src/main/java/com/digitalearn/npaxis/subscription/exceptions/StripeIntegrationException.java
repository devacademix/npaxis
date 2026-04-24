package com.digitalearn.npaxis.subscription.exceptions;

/**
 * Generic Stripe API integration exception wrapping all com.stripe.exception.StripeException subtypes.
 * This unchecked exception allows Stripe API failures to propagate clearly through the application.
 */
public class StripeIntegrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StripeIntegrationException(String message) {
        super(message);
    }

    public StripeIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

}

