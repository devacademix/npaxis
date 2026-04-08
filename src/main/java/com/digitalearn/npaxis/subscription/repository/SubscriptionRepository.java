package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.Subscription;
import com.digitalearn.npaxis.subscription.entity.SubscriptionStatus;
import com.digitalearn.npaxis.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUser(User user);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    boolean existsByUserAndStatusIn(User user, Iterable<SubscriptionStatus> statuses);
}
