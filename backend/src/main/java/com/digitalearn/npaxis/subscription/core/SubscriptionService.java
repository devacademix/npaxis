package com.digitalearn.npaxis.subscription.core;

public interface SubscriptionService {

    CreateCheckoutSessionResponse createCheckoutSession(Long preceptorId, Long priceId);

    SubscriptionStatusResponse getSubscription(Long preceptorId);

    void cancelSubscription(Long preceptorId);

    String createCustomerPortal(Long preceptorId);
}