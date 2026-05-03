package com.digitalearn.npaxis.messaging.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for MessageNotification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<MessageNotification, Long> {

    /**
     * Find all notifications for a user, ordered by creation date descending
     */
    @Query("SELECT n FROM MessageNotification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    Page<MessageNotification> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Count unread notifications for a user
     */
    @Query("SELECT COUNT(n) FROM MessageNotification n WHERE n.user.id = :userId AND n.isRead = false")
    Integer countUnreadByUserId(@Param("userId") Long userId);

}

