package com.digitalearn.npaxis.subscription.service;

import com.digitalearn.npaxis.subscription.dto.CreateSubscriptionRequest;
import com.digitalearn.npaxis.subscription.dto.SubscriptionResponse;
import com.stripe.model.Event;

public interface SubscriptionService {
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);
    void cancelSubscription(Long userId);
    SubscriptionResponse getSubscriptionByUserId(Long userId);
    void processWebhook(Event event);
}
