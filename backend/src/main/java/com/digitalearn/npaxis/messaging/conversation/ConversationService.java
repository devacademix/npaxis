package com.digitalearn.npaxis.messaging.conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing conversations
 */
public interface ConversationService {

    /**
     * Create a new conversation between student and preceptor
     */
    ConversationResponseDTO createConversation(
            Long studentId,
            ConversationRequestDTO requestDTO
    );

    /**
     * Get paginated conversations for a user
     */
    Page<ConversationResponseDTO> getConversations(
            Long userId,
            Pageable pageable
    );

    /**
     * Get paginated conversations filtered by status for a user
     */
    Page<ConversationResponseDTO> getConversationsByStatus(
            Long userId,
            ConversationStatus status,
            Pageable pageable
    );

    /**
     * Get a single conversation by ID
     */
    ConversationResponseDTO getConversation(
            Long conversationId,
            Long userId
    );

    /**
     * Update conversation status
     */
    ConversationResponseDTO updateConversationStatus(
            Long conversationId,
            Long userId,
            ConversationStatus newStatus
    );

    /**
     * Validate if user is a participant of the conversation
     * Throws UnauthorizedParticipantException if not
     */
    void validateUserParticipation(Long conversationId, Long userId);

}
