package com.digitalearn.npaxis.subscription.core;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for subscription event history response
 */
public record SubscriptionEventResponse(
        Long eventId,
        Long subscriptionId,
        String eventType,
        String status,
        LocalDateTime createdAt,
        Map<String, Object> details,
        String stripeEventId,
        String errorMessage
) {
    /**
     * Create from entity
     */
    public static SubscriptionEventResponse fromEntity(SubscriptionEvent event) {
        return new SubscriptionEventResponse(
                event.getSubscriptionEventId(),
                event.getSubscription().getPreceptorSubscriptionId(),
                event.getEventType().toString(),
                event.getStatus().toString(),
                event.getCreatedAt(),
                event.getDetails(),
                event.getStripeEventId(),
                event.getErrorMessage()
        );
    }
}

