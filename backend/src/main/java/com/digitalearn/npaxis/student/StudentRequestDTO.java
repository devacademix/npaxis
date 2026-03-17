package com.digitalearn.npaxis.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for creating/updating a Student.
 */
public record StudentRequestDTO(
        @NotBlank(message = "University is required")
        @Size(max = 100)
        String university,

        @NotBlank(message = "Program is required")
        @Size(max = 100)
        String program,

        @NotBlank(message = "Graduation year is required")
        @Size(min = 4, max = 4)
        String graduationYear,

        @Size(max = 20)
        String phone
) {
}
