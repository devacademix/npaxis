package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin detail view of students
 */
public record AdminStudentDetailDTO(
        Long userId,
        String displayName,
        String email,
        String phone,
        String university,
        String program,
        String graduationYear,
        Integer savedPreceptorsCount,
        Integer inquiriesSentCount,
        LocalDateTime lastActivityDate,
        boolean isEmailVerified,
        boolean isAccountEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

