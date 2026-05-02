package com.digitalearn.npaxis.preceptor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) for returning a Preceptor.
 */
public record PreceptorResponseDTO(
        Long userId,
        String displayName,
        String credentials,
        String specialty,
        String location,
        String setting,
        List<DayOfWeekEnum> availableDays,
        String honorarium,
        String requirements,
        boolean isVerified,
        boolean isPremium,
        String licenseNumber,
        String licenseState,
        String licenseFileUrl,
        VerificationStatus verificationStatus,
        LocalDateTime verificationSubmittedAt,
        LocalDateTime verificationReviewedAt
) {
}
