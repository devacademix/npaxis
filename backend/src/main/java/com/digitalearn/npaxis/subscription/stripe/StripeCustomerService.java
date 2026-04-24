package com.digitalearn.npaxis.subscription.stripe;

import com.stripe.model.Customer;

/**
 * Interface for Stripe Customer management operations.
 * All operations interact directly with the Stripe API using the official SDK.
 * Never parse Stripe JSON manually; always use SDK getter methods.
 */
public interface StripeCustomerService {

    /**
     * Creates a new Stripe Customer for the given preceptor.
     * Stores the returned Stripe customer ID in the preceptor entity.
     *
     * @param userId the preceptor's user ID
     * @param name   the customer name
     * @param email  the customer email
     * @return the Stripe Customer object
     * @throws com.digitalearn.npaxis.subscription.exceptions.StripeIntegrationException if the Stripe API call fails
     */
    Customer createCustomer(Long userId, String name, String email);

    /**
     * Gets or creates a Stripe Customer for the given preceptor.
     * If the preceptor already has a stripeCustomerId, returns it;
     * otherwise creates a new customer and persists the ID.
     *
     * @param userId the preceptor's user ID
     * @return the Stripe customer ID
     * @throws com.digitalearn.npaxis.subscription.exceptions.StripeIntegrationException if the Stripe API call fails
     */
    String getOrCreateCustomer(Long userId);
}


