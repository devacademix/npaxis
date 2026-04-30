package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for verification notes and rejections
 */
public record VerificationNoteDTO(
        Long noteId,
        String note,
        String noteType, // "REVIEW", "REJECTION", "REQUEST_CORRECTION"
        String reviewerName,
        Long reviewedBy,
        LocalDateTime createdAt
) {
}

