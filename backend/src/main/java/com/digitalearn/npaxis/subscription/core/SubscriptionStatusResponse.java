package com.digitalearn.npaxis.subscription.core;

import java.time.LocalDateTime;

public record SubscriptionStatusResponse(
        String stripeSubscriptionId,
        String planCode,
        String billingInterval,
        String status,
        boolean accessEnabled,
        LocalDateTime currentPeriodEnd
) {
}