package com.digitalearn.npaxis.messaging.notification;

import com.digitalearn.npaxis.messaging.exceptions.UnauthorizedParticipantException;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * Implementation of NotificationService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponseDTO createNotification(Long userId, Long conversationId, NotificationType type) {
        log.info("Creating notification for user {} about conversation {} with type {}", userId, conversationId, type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MessageNotification notification = MessageNotification.builder()
                .user(user)
                .conversation(null) // Will be set in MessageServiceImpl
                .notificationType(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        MessageNotification saved = notificationRepository.save(notification);
        log.info("Notification created successfully with ID: {}", saved.getId());

        return notificationMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user {}", userId);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<MessageNotification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return notifications.map(notificationMapper::toResponseDTO);
    }

    @Override
    public void markNotificationAsRead(Long notificationId, Long userId) {
        log.debug("Marking notification {} as read by user {}", notificationId, userId);

        MessageNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found with ID: " + notificationId));

        // Verify user is the owner of the notification
        if (!notification.getUser().getUserId().equals(userId)) {
            log.warn("User {} attempted to mark notification {} of user {} as read",
                    userId, notificationId, notification.getUser().getUserId());
            throw UnauthorizedParticipantException.generic();
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        log.debug("Notification {} marked as read by user {}", notificationId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUnreadNotificationCount(Long userId) {
        log.debug("Getting unread notification count for user {}", userId);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countUnreadByUserId(userId);
    }

}


