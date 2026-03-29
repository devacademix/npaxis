package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Preceptor entities.
 */
@Repository(value = "PreceptorRepository")
public interface PreceptorRepository extends BaseRepository<Preceptor, Long>, JpaSpecificationExecutor<Preceptor> {
    Page<Preceptor> findByVerificationStatus(Pageable pageable, VerificationStatus status);

    Optional<Preceptor> findByUserIdAndIsPremium(Long userId, Boolean isPremium);

    @Query("""
    SELECT e.preceptor.userId, COUNT(e)
    FROM AnalyticsEvent e
    WHERE e.eventType = 'INQUIRY_SENT'
    GROUP BY e.preceptor.userId
    ORDER BY COUNT(e) DESC
""")
    List<Object[]> findTopPreceptors(Pageable pageable);
}
