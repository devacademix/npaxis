package com.digitalearn.npaxis.auditing;

import com.digitalearn.npaxis.preceptor.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity to audit verification status changes for preceptors.
 * Tracks all state transitions with reviewer information and reasons.
 */
@Entity
@Table(
        name = "verification_audit_logs",
        indexes = {
                @Index(name = "idx_verification_audit_preceptor", columnList = "preceptor_id, change_timestamp"),
                @Index(name = "idx_verification_audit_status", columnList = "new_status"),
                @Index(name = "idx_verification_audit_reviewer", columnList = "reviewer_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(name = "preceptor_id", nullable = false)
    private Long preceptorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private VerificationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private VerificationStatus newStatus;

    @Column(name = "reviewer_user_id")
    private Long reviewerUserId;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "change_timestamp", nullable = false)
    private LocalDateTime changeTimestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        if (changeTimestamp == null) {
            this.changeTimestamp = LocalDateTime.now();
        }
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}


