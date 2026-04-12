package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreceptorSubscriptionRepository extends BaseRepository<PreceptorSubscription, Long> {
    Optional<PreceptorSubscription> findByPreceptor_UserId(Long preceptorId);
}