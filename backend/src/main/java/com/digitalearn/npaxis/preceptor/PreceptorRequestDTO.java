package com.digitalearn.npaxis.preceptor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for creating/updating a Preceptor.
 */
public record PreceptorRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String credentials,

        @NotBlank(message = "Specialty is required")
        @Size(max = 100)
        String specialty,

        @NotBlank(message = "Location is required")
        @Size(max = 150)
        String location,

        @Size(max = 100)
        String setting,

        @Size(max = 100)
        String availableDays,

        @Size(max = 100)
        String honorarium,

        String requirements,

        @Email(message = "Email must be valid")
        @Size(max = 100)
        String email,

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
