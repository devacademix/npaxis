package com.digitalearn.npaxis.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.preceptorId = :preceptorId AND e.eventType = :eventType")
    Long countByPreceptorIdAndEventType(@Param("preceptorId") Long preceptorId, @Param("eventType") String eventType);
}
