package com.digitalearn.npaxis.student;

/**
 * Data Transfer Object (DTO) for returning a Student.
 */
public record StudentResponseDTO(
        Long userId,
        String displayName,
        String email,
        String university,
        String program,
        String graduationYear,
        String phone
) {
}
