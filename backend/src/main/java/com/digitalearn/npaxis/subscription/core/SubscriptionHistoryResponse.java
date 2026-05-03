package com.digitalearn.npaxis.subscription.core;

import java.time.LocalDateTime;

public record SubscriptionHistoryResponse(
        Long subscriptionId,
        String planCode,
        String planName,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String cancelReason,
        boolean cancelled,
        LocalDateTime cancelledAt
) {
}

