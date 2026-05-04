package com.digitalearn.npaxis.messaging.notification;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for MessageNotification entity
 */
@Repository
public interface NotificationRepository extends BaseRepository<MessageNotification, Long> {

    /**
     * Find all notifications for a user, ordered by creation date descending
     */
    @Query("SELECT n FROM MessageNotification n WHERE n.user.userId = :userId ORDER BY n.createdAt DESC")
    Page<MessageNotification> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Count unread notifications for a user
     */
    @Query("SELECT COUNT(n) FROM MessageNotification n WHERE n.user.userId = :userId AND n.isRead = false")
    Integer countUnreadByUserId(@Param("userId") Long userId);

}

