package com.digitalearn.npaxis.messaging.conversation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new conversation
 */
public record ConversationRequestDTO(
        @NotNull(message = "Preceptor ID is required")
        Long preceptorId,

        @Size(max = 255, message = "Subject must not exceed 255 characters")
        String subject
) {
}
