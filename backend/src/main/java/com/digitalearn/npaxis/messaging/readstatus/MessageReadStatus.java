package com.digitalearn.npaxis.messaging.readstatus;

import com.digitalearn.npaxis.messaging.message.Message;
import com.digitalearn.npaxis.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * MessageReadStatus Entity
 * <p>
 * Tracks which users have read which messages.
 * This denormalized table improves query performance for common operations like:
 * - Determining unread message counts
 * - Finding unread messages for a user
 */
@Entity
@Table(name = "message_read_statuses", indexes = {
        @Index(name = "idx_read_status_message_id", columnList = "message_id"),
        @Index(name = "idx_read_status_user_id", columnList = "user_id"),
        @Index(name = "idx_read_status_read_at", columnList = "read_at")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"message_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The message that was read
     */
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /**
     * The user who read the message
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Timestamp when the message was read
     */
    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    /**
     * Timestamp when this record was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}

