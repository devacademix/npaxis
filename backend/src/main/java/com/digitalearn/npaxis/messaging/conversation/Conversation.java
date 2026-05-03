package com.digitalearn.npaxis.messaging.conversation;

import com.digitalearn.npaxis.auditing.BaseEntity;
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
 * Conversation Entity
 * <p>
 * Represents a conversation thread between a student and a preceptor.
 * A conversation is initiated by a student and contains multiple messages.
 */
@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversation_student_id", columnList = "student_id"),
        @Index(name = "idx_conversation_preceptor_id", columnList = "preceptor_id"),
        @Index(name = "idx_conversation_status", columnList = "status"),
        @Index(name = "idx_conversation_created_at", columnList = "created_at"),
        @Index(name = "idx_conversation_last_message_at", columnList = "last_message_at"),
        @Index(name = "idx_conversation_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Conversation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Student who initiated the conversation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * Preceptor who is part of the conversation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preceptor_id", nullable = false)
    private User preceptor;

    /**
     * Subject of the conversation
     */
    @Column(length = 255)
    private String subject;

    /**
     * Current status of the conversation
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status = ConversationStatus.OPEN;

    /**
     * Timestamp of the last message in this conversation
     * Used for sorting conversations by recent activity
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

}

