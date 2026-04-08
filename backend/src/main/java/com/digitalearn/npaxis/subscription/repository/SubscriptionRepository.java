package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.SubscriptionEntity;
import com.digitalearn.npaxis.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);
    Optional<SubscriptionEntity> findByUser(User user);
    Optional<SubscriptionEntity> findTopByUserOrderByCreatedAtDesc(User user);
}
