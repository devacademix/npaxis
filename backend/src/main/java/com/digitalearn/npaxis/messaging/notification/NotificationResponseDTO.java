package com.digitalearn.npaxis.messaging.notification;

import java.time.LocalDateTime;

/**
 * DTO for responding with notification details
 */
public record NotificationResponseDTO(
        Long id,
        Long userId,
        Long conversationId,
        String conversationSubject,
        NotificationType type,
        String message,
        Boolean isRead,
        LocalDateTime createdAt
) {
}
