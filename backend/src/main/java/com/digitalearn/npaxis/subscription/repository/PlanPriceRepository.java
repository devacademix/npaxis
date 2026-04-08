package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.PlanPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlanPriceRepository extends JpaRepository<PlanPrice, Long> {
    Optional<PlanPrice> findByStripePriceId(String stripePriceId);
}
