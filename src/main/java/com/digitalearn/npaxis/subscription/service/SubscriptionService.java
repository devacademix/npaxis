package com.digitalearn.npaxis.subscription.service;

import com.digitalearn.npaxis.subscription.dto.CreateSubscriptionRequest;
import com.digitalearn.npaxis.subscription.dto.SubscriptionResponse;
import com.digitalearn.npaxis.subscription.entity.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionService {
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);

    SubscriptionResponse cancelAtPeriodEnd(UUID userId);

    Optional<Subscription> findByUserId(UUID userId);
}
