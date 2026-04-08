package com.digitalearn.npaxis.subscription.dto;

import com.digitalearn.npaxis.subscription.entity.SubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        String stripeSubscriptionId,
        String stripePriceId,
        SubscriptionStatus status,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd
) {
}
