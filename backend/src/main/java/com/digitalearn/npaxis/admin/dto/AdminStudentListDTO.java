package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin list view of students
 */
public record AdminStudentListDTO(
        Long userId,
        String displayName,
        String email,
        String university,
        String program,
        String graduationYear,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

