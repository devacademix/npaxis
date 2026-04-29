package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for webhook event details
 */
public record WebhookEventDetailDTO(
        String eventId,
        String eventType,
        String status, // "SUCCEEDED", "FAILED", "RETRYING"
        String eventPayload,
        Integer retryCount,
        LocalDateTime eventDate,
        LocalDateTime lastUpdated
) {
}

