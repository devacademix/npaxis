package com.digitalearn.npaxis.messaging.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing notifications
 */
public interface NotificationService {

    /**
     * Create a new notification
     */
    NotificationResponseDTO createNotification(
            Long userId,
            Long conversationId,
            NotificationType type
    );

    /**
     * Get paginated notifications for a user
     */
    Page<NotificationResponseDTO> getNotifications(
            Long userId,
            Pageable pageable
    );

    /**
     * Mark a notification as read
     */
    void markNotificationAsRead(Long notificationId, Long userId);

    /**
     * Get count of unread notifications for a user
     */
    Integer getUnreadNotificationCount(Long userId);

}
