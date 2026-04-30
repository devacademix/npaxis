package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for verification history events
 */
public record VerificationHistoryDTO(
        Long historyId,
        String action,
        String oldStatus,
        String newStatus,
        String reviewerName,
        Long reviewedBy,
        String notes,
        LocalDateTime createdAt
) {
}

