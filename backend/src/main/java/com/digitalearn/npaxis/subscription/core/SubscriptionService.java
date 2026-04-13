package com.digitalearn.npaxis.subscription.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing preceptor subscriptions
 */
public interface SubscriptionService {

    /**
     * Create a Stripe checkout session for subscription
     *
     * @param userId  the preceptor user ID
     * @param priceId the subscription price ID
     * @return checkout session details
     */
    CreateCheckoutSessionResponse createCheckoutSession(Long userId, Long priceId);

    /**
     * Get current subscription details
     *
     * @param userId the preceptor user ID
     * @return subscription detail response
     */
    SubscriptionDetailResponse getSubscriptionDetail(Long userId);

    /**
     * Cancel subscription at period end (graceful cancellation)
     * User can still use service until currentPeriodEnd
     *
     * @param userId the preceptor user ID
     */
    void cancelSubscription(Long userId);

    /**
     * Update subscription to a different plan/price
     *
     * @param userId  the preceptor user ID
     * @param request the update request with new price ID
     */
    void updateSubscription(Long userId, UpdateSubscriptionRequest request);

    /**
     * Get subscription history with pagination
     *
     * @param userId   the preceptor user ID
     * @param pageable pagination details
     * @return paginated subscription history
     */
    Page<SubscriptionHistoryResponse> getSubscriptionHistory(Long userId, Pageable pageable);

    /**
     * Create a Stripe customer portal session for billing management
     *
     * @param userId the preceptor user ID
     * @return portal session URL
     */
    String createCustomerPortal(Long userId);

    /**
     * Check if user can access premium features
     * Includes grace period checking for canceled subscriptions
     *
     * @param userId the preceptor user ID
     * @return true if user has valid subscription access
     */
    boolean canAccessPremiumFeatures(Long userId);

    /**
     * Sync local subscription from Stripe subscription data
     * Called by webhook handlers
     *
     * @param stripeSubscriptionId the Stripe subscription ID
     */
    void syncLocalSubscriptionFromStripe(String stripeSubscriptionId);
}

