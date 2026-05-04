package com.digitalearn.npaxis.messaging.message;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Message entity
 */
@Repository
public interface MessageRepository extends BaseRepository<Message, Long> {

    /**
     * Find all messages in a conversation, ordered by creation date descending
     */
    @Query("SELECT m FROM Message m WHERE " +
            "m.conversation.id = :conversationId AND m.deleted = false " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findByConversationId(
            @Param("conversationId") Long conversationId,
            Pageable pageable
    );

    /**
     * Find unread messages in a conversation for a specific user
     */
    @Query("SELECT m FROM Message m WHERE " +
            "m.conversation.id = :conversationId AND " +
            "m.sender.userId != :userId AND " +
            "m.readAt IS NULL AND " +
            "m.deleted = false " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findUnreadMessagesForUser(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Count unread messages in a conversation for a specific user
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
            "m.conversation.id = :conversationId AND " +
            "m.sender.userId != :userId AND " +
            "m.readAt IS NULL AND " +
            "m.deleted = false")
    Integer countUnreadMessagesForUser(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

}

