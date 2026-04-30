package com.digitalearn.npaxis.admin.dto;

import com.digitalearn.npaxis.preceptor.VerificationStatus;

import java.time.LocalDateTime;

/**
 * DTO for admin list view of preceptors - minimal data exposure
 */
public record AdminPreceptorListDTO(
        Long userId,
        String displayName,
        String specialty,
        String location,
        boolean isVerified,
        boolean isPremium,
        VerificationStatus verificationStatus,
        LocalDateTime verificationSubmittedAt,
        LocalDateTime verificationReviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

