package com.digitalearn.npaxis.subscription.core;

import java.time.LocalDateTime;

public record SubscriptionDetailResponse(
        Long subscriptionId,
        String planCode,
        String planName,
        String billingInterval,
        Long amountInMinorUnits,
        String currency,
        String status,
        boolean accessEnabled,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime trialEndsAt,
        boolean cancelAtPeriodEnd,
        LocalDateTime canceledAt,
        String canceledReason,
        boolean cancelled,
        LocalDateTime nextBillingDate
) {
}

