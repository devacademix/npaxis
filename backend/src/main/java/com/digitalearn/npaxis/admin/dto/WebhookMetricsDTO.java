package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for webhook metrics and statistics
 */
public record WebhookMetricsDTO(
    Long successfulCount,
    Long failedRetryingCount,
    Long failedCount,
    Double successRate,
    Integer averageRetryCount,
    LocalDateTime oldestPendingEventDate,
    String mostCommonEventType,
    LocalDateTime reportGeneratedAt
) {
}

