package com.digitalearn.npaxis.admin.dto;

import com.digitalearn.npaxis.preceptor.VerificationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for admin detail view of preceptors - full admin visibility
 */
public record AdminPreceptorDetailDTO(
        Long userId,
        String displayName,
        String email,
        String phone,
        String credentials,
        String specialty,
        String location,
        String setting,
        List<String> availableDays,
        String honorarium,
        String requirements,
        boolean isVerified,
        boolean isPremium,
        String licenseNumber,
        String licenseState,
        String licenseFileUrl,
        VerificationStatus verificationStatus,
        LocalDateTime verificationSubmittedAt,
        LocalDateTime verificationReviewedAt,
        List<VerificationNoteDTO> verificationNotes,
        List<VerificationHistoryDTO> verificationHistory,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

