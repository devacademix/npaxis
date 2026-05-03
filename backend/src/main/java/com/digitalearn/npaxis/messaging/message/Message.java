package com.digitalearn.npaxis.messaging.message;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.messaging.conversation.Conversation;
import com.digitalearn.npaxis.role.RoleName;
import com.digitalearn.npaxis.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Message Entity
 * <p>
 * Represents a single message within a conversation.
 * Messages are immutable once created (no updates, only soft deletes).
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_message_sender_id", columnList = "sender_id"),
        @Index(name = "idx_message_created_at", columnList = "created_at"),
        @Index(name = "idx_message_read_at", columnList = "read_at"),
        @Index(name = "idx_message_deleted", columnList = "deleted"),
        @Index(name = "idx_message_conversation_created", columnList = "conversation_id,created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Conversation this message belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * User who sent this message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Role of the sender at the time the message was sent
     * Used for display purposes and audit trail
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private RoleName senderRole;

    /**
     * The message content
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Timestamp when this message was marked as read by the recipient
     * null if not yet read
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

}

