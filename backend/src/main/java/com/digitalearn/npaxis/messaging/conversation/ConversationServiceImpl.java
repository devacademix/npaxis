package com.digitalearn.npaxis.messaging.conversation;

import com.digitalearn.npaxis.messaging.exceptions.ConversationNotFoundException;
import com.digitalearn.npaxis.messaging.exceptions.UnauthorizedParticipantException;
import com.digitalearn.npaxis.messaging.message.MessageRepository;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ConversationService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;

    @Override
    public ConversationResponseDTO createConversation(Long studentId, ConversationRequestDTO requestDTO) {
        log.info("Creating conversation for student {} with preceptor {}", studentId, requestDTO.preceptorId());

        // Verify student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Verify preceptor exists
        User preceptor = userRepository.findById(requestDTO.preceptorId())
                .orElseThrow(() -> new RuntimeException("Preceptor not found"));

        // Check if conversation already exists between these two users
        var existing = conversationRepository.findExistingConversation(studentId, requestDTO.preceptorId());
        if (existing.isPresent()) {
            log.debug("Conversation already exists between student {} and preceptor {}", studentId, requestDTO.preceptorId());
            Conversation conv = existing.get();
            return enrich(conversationMapper.toResponseDTO(conv), conv, studentId);
        }

        // Create new conversation
        Conversation conversation = Conversation.builder()
                .student(student)
                .preceptor(preceptor)
                .subject(requestDTO.subject())
                .status(ConversationStatus.OPEN)
                .build();

        Conversation saved = conversationRepository.save(conversation);
        log.info("Conversation created successfully with ID: {}", saved.getId());

        return enrich(conversationMapper.toResponseDTO(saved), saved, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponseDTO> getConversations(Long userId, Pageable pageable) {
        log.debug("Fetching conversations for user {}", userId);
        Page<Conversation> conversations = conversationRepository.findUserConversations(userId, pageable);
        return conversations.map(c -> enrich(conversationMapper.toResponseDTO(c), c, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponseDTO> getConversationsByStatus(
            Long userId,
            ConversationStatus status,
            Pageable pageable) {
        log.debug("Fetching conversations for user {} with status {}", userId, status);
        Page<Conversation> conversations = conversationRepository.findUserConversationsByStatus(userId, status, pageable);
        return conversations.map(c -> enrich(conversationMapper.toResponseDTO(c), c, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponseDTO getConversation(Long conversationId, Long userId) {
        log.debug("Fetching conversation {} for user {}", conversationId, userId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ConversationNotFoundException.withId(conversationId));

        validateUserParticipation(conversationId, userId);

        return enrich(conversationMapper.toResponseDTO(conversation), conversation, userId);
    }

    @Override
    public ConversationResponseDTO updateConversationStatus(
            Long conversationId,
            Long userId,
            ConversationStatus newStatus) {
        log.info("Updating conversation {} status to {}", conversationId, newStatus);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ConversationNotFoundException.withId(conversationId));

        validateUserParticipation(conversationId, userId);

        conversation.setStatus(newStatus);
        Conversation updated = conversationRepository.save(conversation);

        log.info("Conversation {} status updated to {}", conversationId, newStatus);
        return enrich(conversationMapper.toResponseDTO(updated), updated, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateUserParticipation(Long conversationId, Long userId) {
        Boolean isParticipant = conversationRepository.isUserParticipant(conversationId, userId);
        if (!isParticipant) {
            log.warn("User {} is not a participant of conversation {}", userId, conversationId);
            throw UnauthorizedParticipantException.forConversation(conversationId, userId);
        }
    }

    /**
     * Enrich conversation DTO with unread count and last message preview
     */
    private ConversationResponseDTO enrich(ConversationResponseDTO dto, Conversation conversation, Long userId) {
        Integer unreadCount = conversationRepository.countUnreadMessagesForUser(conversation.getId(), userId);
        String lastMessagePreview = getLastMessagePreview(conversation.getId());

        return new ConversationResponseDTO(
                dto.id(),
                dto.studentId(),
                dto.studentName(),
                dto.preceptorId(),
                dto.preceptorName(),
                dto.subject(),
                dto.status(),
                dto.lastMessageAt(),
                unreadCount != null ? unreadCount : 0,
                lastMessagePreview,
                dto.createdAt(),
                dto.lastModifiedAt()
        );
    }

    /**
     * Get last message preview for a conversation
     */
    private String getLastMessagePreview(Long conversationId) {
        var messages = messageRepository.findByConversationId(conversationId,
                org.springframework.data.domain.PageRequest.of(0, 1));
        if (messages.hasContent()) {
            String content = messages.getContent().get(0).getContent();
            int maxLength = 100;
            return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
        }
        return null;
    }

}


