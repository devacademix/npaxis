package com.digitalearn.npaxis.messaging.conversation;

import java.time.LocalDateTime;

/**
 * DTO for responding with conversation details
 */
public record ConversationResponseDTO(
        Long id,
        Long studentId,
        String studentName,
        Long preceptorId,
        String preceptorName,
        String subject,
        ConversationStatus status,
        LocalDateTime lastMessageAt,
        Integer unreadCount,
        String lastMessagePreview,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt
) {
}

