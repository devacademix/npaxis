package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.BillingDetails;
import com.digitalearn.npaxis.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BillingDetailsRepository extends JpaRepository<BillingDetails, UUID> {
    Optional<BillingDetails> findByUser(User user);
}
