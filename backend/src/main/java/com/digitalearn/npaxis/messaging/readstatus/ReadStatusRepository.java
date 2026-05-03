package com.digitalearn.npaxis.messaging.readstatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for MessageReadStatus entity
 */
@Repository
public interface ReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {

    /**
     * Find read status for a specific message and user
     */
    @Query("SELECT r FROM MessageReadStatus r WHERE r.message.id = :messageId AND r.user.id = :userId")
    Optional<MessageReadStatus> findByMessageIdAndUserId(
            @Param("messageId") Long messageId,
            @Param("userId") Long userId
    );

    /**
     * Check if a user has read a message
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM MessageReadStatus r " +
            "WHERE r.message.id = :messageId AND r.user.id = :userId")
    Boolean hasUserReadMessage(
            @Param("messageId") Long messageId,
            @Param("userId") Long userId
    );

}

