package com.digitalearn.npaxis.preceptor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) for returning a Preceptor.
 */
public record PreceptorResponseDTO(
        Long userId,
        String displayName,
//        String email,
//        String name,
        String credentials,
        String specialty,
        String location,
        String setting,
        List<DayOfWeekEnum> availableDays,
        String honorarium,
        String requirements,
//        String preceptorEmail,
//        String phone,
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
