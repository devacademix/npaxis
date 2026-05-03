package com.digitalearn.npaxis.messaging.message;

import com.digitalearn.npaxis.role.RoleName;

import java.time.LocalDateTime;

/**
 * DTO for responding with message details
 */
public record MessageResponseDTO(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        RoleName senderRole,
        String content,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}

