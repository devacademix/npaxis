package com.digitalearn.npaxis.analytics;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsEventRepository extends BaseRepository<AnalyticsEvent, Long> {
    /**
     * Count events grouped by type for a preceptor.
     */
    @Query("""
                SELECT e.eventType, COUNT(e)
                FROM AnalyticsEvent e
                WHERE e.preceptor.userId = :preceptorId
                GROUP BY e.eventType
            """)
    List<Object[]> countEventsByPreceptor(Long preceptorId);

    @Query("""
            SELECT e.eventType, COUNT(e)
            FROM AnalyticsEvent e
            GROUP BY e.eventType
            """)
    List<Object[]> countAllEvents();

    @Query("""
            SELECT
                p.user.userId AS preceptorId,
                p.user.displayName AS displayName,
                COUNT(e) AS inquiryCount
            FROM AnalyticsEvent e
            JOIN e.preceptor p
            WHERE e.eventType = :eventType
            GROUP BY p.user.userId, p.user.displayName
            ORDER BY COUNT(e) DESC
            """)
    List<TopPreceptorProjection> findTopPreceptors(@Param("eventType") EventType eventType, Pageable pageable);
}