package com.digitalearn.npaxis.subscription.dto;

import com.digitalearn.npaxis.subscription.entity.SubscriptionStatus;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long id,
        Long userId,
        String stripeSubscriptionId,
        String planName,
        String stripePriceId,
        SubscriptionStatus status,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        boolean cancelAtPeriodEnd
) {}
