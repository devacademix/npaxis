package com.digitalearn.npaxis.messaging.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing messages
 */
public interface MessageService {

    /**
     * Send a new message in a conversation
     */
    MessageResponseDTO sendMessage(
            Long conversationId,
            Long senderId,
            MessageRequestDTO requestDTO
    );

    /**
     * Get paginated messages in a conversation
     */
    Page<MessageResponseDTO> getMessages(
            Long conversationId,
            Long userId,
            Pageable pageable
    );

    /**
     * Mark a message as read
     */
    void markMessageAsRead(Long messageId, Long userId);

    /**
     * Get count of unread messages in a conversation for a user
     */
    Integer getUnreadMessageCount(Long conversationId, Long userId);

    /**
     * Validate if user is a participant of the conversation
     * Throws UnauthorizedParticipantException if not
     */
    void validateUserParticipation(Long conversationId, Long userId);

}
