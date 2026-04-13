package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreceptorSubscriptionRepository extends BaseRepository<PreceptorSubscription, Long> {

    Optional<PreceptorSubscription> findByPreceptor_UserId(Long preceptorId);

    Optional<PreceptorSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Page<PreceptorSubscription> findByPreceptor_UserIdOrderByCreatedAtDesc(Long preceptorId, Pageable pageable);

    List<PreceptorSubscription> findByStatus(SubscriptionStatus status);

    List<PreceptorSubscription> findByCancelAtPeriodEndTrue();

    List<PreceptorSubscription> findByCurrentPeriodEndBefore(LocalDateTime dateTime);

    Optional<PreceptorSubscription> findByStripeCustomerId(String stripeCustomerId);
}