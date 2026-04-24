package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.subscription.price.SubscriptionPrice;
import com.digitalearn.npaxis.subscription.price.SubscriptionPriceRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionRetryService {

    private final PreceptorSubscriptionRepository subscriptionRepository;
    private final SubscriptionPriceRepository priceRepository;
    private final EntityManager entityManager;  // inject this

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertInNewTransaction(
            Long preceptorId,
            String stripeSubscriptionId,
            String stripePriceId,
            SubscriptionStatus status,
            Boolean cancelAtPeriodEnd,
            LocalDateTime currentPeriodStart,
            LocalDateTime currentPeriodEnd,
            LocalDateTime nextBillingDate
    ) {
        Optional<PreceptorSubscription> existingOpt =
                subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);

        PreceptorSubscription subscription;

        if (existingOpt.isPresent()) {
            subscription = existingOpt.get();
        } else {
            subscription = new PreceptorSubscription();
            subscription.setStripeSubscriptionId(stripeSubscriptionId);
            Preceptor preceptor = entityManager.getReference(Preceptor.class, preceptorId);
            subscription.setPreceptor(preceptor);
        }

        SubscriptionPrice price = priceRepository
                .findByStripePriceId(stripePriceId)
                .orElseThrow(() -> new IllegalStateException("Price not found: " + stripePriceId));

        subscription.setPrice(price);
        subscription.setPlan(price.getPlan());  // Extract plan from price (NOT NULL constraint)
        subscription.setStatus(status);
        subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        subscription.setCurrentPeriodStart(currentPeriodStart);
        subscription.setCurrentPeriodEnd(currentPeriodEnd);
        subscription.setNextBillingDate(nextBillingDate);

        try {
            subscriptionRepository.saveAndFlush(subscription);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Race condition on insert for subscription: {}, updating existing",
                    stripeSubscriptionId);

            // *** THE KEY FIX: clear the broken entity from the session
            // before attempting any further queries in this transaction
            entityManager.clear();

            PreceptorSubscription existing = subscriptionRepository
                    .findByStripeSubscriptionId(stripeSubscriptionId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Subscription not found after conflict: " + stripeSubscriptionId));

            existing.setStatus(status);
            existing.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            existing.setCurrentPeriodStart(currentPeriodStart);
            existing.setCurrentPeriodEnd(currentPeriodEnd);
            existing.setNextBillingDate(nextBillingDate);

            // Re-attach the price in the fresh session context
            SubscriptionPrice freshPrice = priceRepository
                    .findByStripePriceId(stripePriceId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Price not found: " + stripePriceId));
            existing.setPrice(freshPrice);
            existing.setPlan(freshPrice.getPlan());  // Extract plan from price (NOT NULL constraint)

            subscriptionRepository.save(existing);
        }
    }
}