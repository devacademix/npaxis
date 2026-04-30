package com.digitalearn.npaxis.subscription.stripe;

/**
 * Interface for Stripe Subscription management operations.
 * All operations interact directly with the Stripe API using the official SDK.
 * Never parse Stripe JSON manually; always use SDK getter methods.
 */
public interface StripeSubscriptionService {

    /**
     * Creates a checkout session for subscription purchase.
     * Returns the session URL for redirecting the customer to Stripe's hosted checkout.
     *
     * @param userId  the preceptor's user ID
     * @param priceId the local SubscriptionPrice ID
     * @return the Stripe checkout session URL
     * @throws com.digitalearn.npaxis.subscription.exceptions.StripeIntegrationException if the Stripe API call fails
     */
    String createCheckoutSession(Long userId, Long priceId);

    /**
     * Creates a customer portal session URL.
     * Allows the customer to manage their subscription directly in Stripe's portal.
     *
     * @param stripeCustomerId the Stripe customer ID
     * @return the customer portal session URL
     * @throws com.digitalearn.npaxis.subscription.exceptions.StripeIntegrationException if the Stripe API call fails
     */
    String createCustomerPortalSession(String stripeCustomerId);
}

