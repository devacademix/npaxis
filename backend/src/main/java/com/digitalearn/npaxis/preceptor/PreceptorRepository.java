package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Preceptor entities.
 */
@Repository(value = "PreceptorRepository")
public interface PreceptorRepository extends BaseRepository<Preceptor, Long> {
    List<Preceptor> findByVerificationStatus(VerificationStatus status);
    Optional<Preceptor> findByUserIdAndIsPremium(Long userId, Boolean isPremium);
}
