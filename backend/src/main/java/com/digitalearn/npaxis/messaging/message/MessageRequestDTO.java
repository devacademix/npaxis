package com.digitalearn.npaxis.messaging.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for sending a new message
 */
public record MessageRequestDTO(
        @NotBlank(message = "Message content is required")
        @Size(max = 10000, message = "Message content must not exceed 10000 characters")
        String content
) {
}
