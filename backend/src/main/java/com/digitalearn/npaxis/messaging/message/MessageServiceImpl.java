package com.digitalearn.npaxis.messaging.message;

import com.digitalearn.npaxis.messaging.conversation.Conversation;
import com.digitalearn.npaxis.messaging.conversation.ConversationRepository;
import com.digitalearn.npaxis.messaging.conversation.ConversationStatus;
import com.digitalearn.npaxis.messaging.exceptions.ConversationNotFoundException;
import com.digitalearn.npaxis.messaging.exceptions.InvalidConversationStateException;
import com.digitalearn.npaxis.messaging.exceptions.MessageNotFoundException;
import com.digitalearn.npaxis.messaging.exceptions.UnauthorizedParticipantException;
import com.digitalearn.npaxis.messaging.notification.MessageNotification;
import com.digitalearn.npaxis.messaging.notification.NotificationRepository;
import com.digitalearn.npaxis.messaging.notification.NotificationType;
import com.digitalearn.npaxis.messaging.readstatus.MessageReadStatus;
import com.digitalearn.npaxis.messaging.readstatus.ReadStatusRepository;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of MessageService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationRepository notificationRepository;
    private final MessageMapper messageMapper;

    @Override
    public MessageResponseDTO sendMessage(Long conversationId, Long senderId, MessageRequestDTO requestDTO) {
        log.info("Sending message to conversation {} from user {}", conversationId, senderId);

        // Get conversation
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ConversationNotFoundException.withId(conversationId));

        // Validate user is participant
        validateUserParticipation(conversationId, senderId);

        // Validate conversation is not closed
        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            throw InvalidConversationStateException.cannotSendToClosedConversation(conversationId);
        }

        // Get sender
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .senderRole(sender.getRole().getRoleName())
                .content(requestDTO.content())
                .build();

        Message saved = messageRepository.save(message);

        // Update conversation's last message timestamp
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Create notification for the other participant
        createNotificationForRecipient(conversation, senderId);

        log.info("Message sent successfully with ID: {}", saved.getId());
        return messageMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getMessages(Long conversationId, Long userId, Pageable pageable) {
        log.debug("Fetching messages for conversation {} for user {}", conversationId, userId);

        // Validate user is participant
        validateUserParticipation(conversationId, userId);

        // Mark all unread messages as read for this conversation for the user
        Page<Message> messages = messageRepository.findByConversationId(conversationId, pageable);
        messages.getContent().forEach(msg -> {
            if (msg.getReadAt() == null && !msg.getSender().getUserId().equals(userId)) {
                markMessageAsRead(msg.getId(), userId);
            }
        });

        // Fetch messages again (or could use streams)
        messages = messageRepository.findByConversationId(conversationId, pageable);
        return messages.map(messageMapper::toResponseDTO);
    }

    @Override
    public void markMessageAsRead(Long messageId, Long userId) {
        log.debug("Marking message {} as read by user {}", messageId, userId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> MessageNotFoundException.withId(messageId));

        // Validate user is participant of the conversation
        validateUserParticipation(message.getConversation().getId(), userId);

        // Check if already read
        var existingReadStatus = readStatusRepository.findByMessageIdAndUserId(messageId, userId);
        if (existingReadStatus.isPresent()) {
            log.debug("Message {} already marked as read by user {}", messageId, userId);
            return;
        }

        // Create read status
        MessageReadStatus readStatus = MessageReadStatus.builder()
                .message(message)
                .user(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")))
                .readAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        readStatusRepository.save(readStatus);

        log.debug("Message {} marked as read by user {}", messageId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUnreadMessageCount(Long conversationId, Long userId) {
        log.debug("Getting unread message count for conversation {} for user {}", conversationId, userId);
        validateUserParticipation(conversationId, userId);
        return messageRepository.countUnreadMessagesForUser(conversationId, userId);
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
     * Create notification for the other participant when a message is sent
     */
    private void createNotificationForRecipient(Conversation conversation, Long senderId) {
        // Determine who is the recipient
        Long recipientId = conversation.getStudent().getUserId().equals(senderId) ?
                conversation.getPreceptor().getUserId() :
                conversation.getStudent().getUserId();

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient user not found"));

        // Create notification
        MessageNotification notification = MessageNotification.builder()
                .user(recipient)
                .conversation(conversation)
                .notificationType(NotificationType.NEW_MESSAGE)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.debug("Notification created for user {} about conversation {}", recipientId, conversation.getId());
    }

}





