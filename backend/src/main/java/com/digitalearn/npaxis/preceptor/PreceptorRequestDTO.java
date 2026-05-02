package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.validation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data Transfer Object (DTO) for creating/updating a Preceptor.
 */
public record PreceptorRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        /**
         * List of credential names (case-insensitive).
         * Duplicates will be automatically prevented (e.g., "MBBS", "mbbs" treated as one).
         * If a credential doesn't exist, it will be created automatically.
         */
        @Size(max = 10, message = "A preceptor can have at most 10 credentials")
        List<String> credentials,

        /**
         * List of specialty names (case-insensitive).
         * Duplicates will be automatically prevented (e.g., "Cardiology", "cardiology" treated as one).
         * If a specialty doesn't exist, it will be created automatically.
         * At least one specialty is required.
         */
        @NotBlank(message = "At least one specialty is required")
        @Size(max = 5, message = "A preceptor can have at most 5 specialties")
        List<String> specialties,

        @NotBlank(message = "Location is required")
        @Size(max = 150)
        String location,

        @Size(max = 100)
        String setting,

        @Size(max = 100)
        List<DayOfWeekEnum> availableDays,

        @Size(max = 100)
        String honorarium,

        String requirements,

        @Email(message = "Email must be a valid email format")
        @Size(max = 100)
        String email,

        @ValidPhone
        @Size(max = 20)
        String phone,

        @Size(max = 100)
        String licenseNumber,

        @Size(max = 50)
        String licenseState,

        @Size(max = 500)
        String licenseFileUrl
) {
}
