package com.digitalearn.npaxis.webhook;

import java.time.LocalDateTime;

public record WebhookEventResponse(
        String eventId,
        String eventType,
        String status,
        LocalDateTime processedAt,
        Integer retryCount,
        String errorMessage
) {
}

