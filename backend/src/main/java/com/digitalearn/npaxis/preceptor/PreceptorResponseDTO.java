package com.digitalearn.npaxis.preceptor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) for returning a Preceptor.
 */
public record PreceptorResponseDTO(
        Long userId,
        String displayName,
        /**
         * List of credential names (e.g., ["MBBS", "MD", "Ph.D"])
         */
        List<String> credentials,
        /**
         * List of specialty names (e.g., ["Cardiology", "Internal Medicine"])
         */
        List<String> specialties,
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
