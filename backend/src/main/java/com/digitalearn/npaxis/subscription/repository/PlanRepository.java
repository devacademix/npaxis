package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByName(String name);
}
