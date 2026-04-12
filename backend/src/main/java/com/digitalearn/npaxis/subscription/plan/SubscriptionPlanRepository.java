package com.digitalearn.npaxis.subscription.plan;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends BaseRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByCode(String code);
}