package com.digitalearn.npaxis.auditing;

import com.digitalearn.npaxis.preceptor.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing VerificationAuditLog entities.
 * Provides methods for querying verification status change history.
 */
@Repository
public interface VerificationAuditLogRepository extends BaseRepository<VerificationAuditLog, Long> {

    /**
     * Find all audit logs for a specific preceptor, paginated.
     */
    Page<VerificationAuditLog> findByPreceptorIdAndDeletedFalseOrderByChangeTimestampDesc(
            Long preceptorId, Pageable pageable);

    /**
     * Find all audit logs for a preceptor within a date range.
     */
    List<VerificationAuditLog> findByPreceptorIdAndChangeTimestampBetweenAndDeletedFalse(
            Long preceptorId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find the latest audit log entry for a preceptor.
     */
    Optional<VerificationAuditLog> findFirstByPreceptorIdAndDeletedFalseOrderByChangeTimestampDesc(Long preceptorId);

    /**
     * Find all audit logs by status change, paginated.
     */
    Page<VerificationAuditLog> findByNewStatusAndDeletedFalseOrderByChangeTimestampDesc(
            VerificationStatus status, Pageable pageable);

    /**
     * Find audit logs by reviewer ID.
     */
    Page<VerificationAuditLog> findByReviewerUserIdAndDeletedFalseOrderByChangeTimestampDesc(
            Long reviewerUserId, Pageable pageable);

    /**
     * Count total audit logs for a preceptor (non-deleted only).
     */
    long countByPreceptorIdAndDeletedFalse(Long preceptorId);

    /**
     * Find all audit logs for a preceptor with a specific status transition.
     */
    @Query("""
            SELECT log FROM VerificationAuditLog log
            WHERE log.preceptorId = :preceptorId
            AND log.previousStatus = :previousStatus
            AND log.newStatus = :newStatus
            AND log.deleted = false
            ORDER BY log.changeTimestamp DESC
            """)
    List<VerificationAuditLog> findTransitions(
            @Param("preceptorId") Long preceptorId,
            @Param("previousStatus") VerificationStatus previousStatus,
            @Param("newStatus") VerificationStatus newStatus);
}


