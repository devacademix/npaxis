package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.PlanPrice;
import com.digitalearn.npaxis.subscription.entity.PlanInterval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanPriceRepository extends JpaRepository<PlanPrice, Long> {
    Optional<PlanPrice> findByStripePriceIdAndActiveTrue(String stripePriceId);

    Optional<PlanPrice> findFirstByPlanIdAndIntervalAndActiveTrueOrderByCreatedAtDesc(Long planId, PlanInterval interval);
}
