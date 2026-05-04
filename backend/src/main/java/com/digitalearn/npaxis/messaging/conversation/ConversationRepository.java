package com.digitalearn.npaxis.messaging.conversation;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Conversation entity
 */
@Repository
public interface ConversationRepository extends BaseRepository<Conversation, Long> {

    /**
     * Find conversations for a user (either as student or preceptor)
     */
    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.student.userId = :userId OR c.preceptor.userId = :userId) AND c.deleted = false " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findUserConversations(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find conversations by status for a user
     */
    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.student.userId = :userId OR c.preceptor.userId = :userId) AND " +
            "c.status = :status AND c.deleted = false " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findUserConversationsByStatus(
            @Param("userId") Long userId,
            @Param("status") ConversationStatus status,
            Pageable pageable
    );

    /**
     * Find existing conversation between student and preceptor
     */
    @Query("SELECT c FROM Conversation c WHERE " +
            "c.student.userId = :studentId AND c.preceptor.userId = :preceptorId AND " +
            "c.deleted = false")
    Optional<Conversation> findExistingConversation(
            @Param("studentId") Long studentId,
            @Param("preceptorId") Long preceptorId
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

    /**
     * Verify if user is a participant of the conversation
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Conversation c WHERE " +
            "c.id = :conversationId AND " +
            "(c.student.userId = :userId OR c.preceptor.userId = :userId) AND " +
            "c.deleted = false")
    Boolean isUserParticipant(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

}



