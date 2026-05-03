package com.digitalearn.npaxis.subscription.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Validates subscription access for premium features.
 * Access is granted if:
 * 1. Subscription is ACTIVE or TRIALING and not expired
 * 2. Subscription is CANCELED but currentPeriodEnd hasn't passed yet (grace period)
 */
@Component
@RequiredArgsConstructor
public class SubscriptionAccessValidator {

    private final PreceptorSubscriptionRepository subscriptionRepo;

    /**
     * Check if preceptor has an active/valid subscription
     */
    public boolean hasActiveSubscription(Long preceptorId) {
        return subscriptionRepo.findByPreceptor_UserIdAndActiveTrue(preceptorId)
                .map(sub -> {
                    if (sub.getStatus() != SubscriptionStatus.ACTIVE &&
                            sub.getStatus() != SubscriptionStatus.TRIALING) {
                        return false;
                    }

                    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

                    // Check if trial has expired
                    if (sub.getStatus() == SubscriptionStatus.TRIALING &&
                            sub.getTrialEndsAt() != null && sub.getTrialEndsAt().isBefore(now)) {
                        return false;
                    }

                    return true;
                })
                .orElse(false);
    }

    /**
     * Check if preceptor can access features even after cancellation
     * (until period end)
     */
    public boolean canAccessUntilPeriodEnd(Long preceptorId) {
        return subscriptionRepo.findByPreceptor_UserIdAndActiveTrue(preceptorId)
                .map(sub -> {
                    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

                    if (sub.getCurrentPeriodEnd() != null &&
                            sub.getCurrentPeriodEnd().isAfter(now)) {
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Combined check: can access if ACTIVE or if CANCELED within grace period
     */
    public boolean canAccessPremiumFeatures(Long preceptorId) {
        return subscriptionRepo.findByPreceptor_UserIdAndActiveTrue(preceptorId)
                .map(sub -> {
                    // Must be access enabled
                    if (!sub.isAccessEnabled()) {
                        return false;
                    }

                    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

                    // If ACTIVE or TRIALING, check if grace period applies
                    if (sub.getStatus() == SubscriptionStatus.ACTIVE ||
                            sub.getStatus() == SubscriptionStatus.TRIALING) {

                        if (sub.getStatus() == SubscriptionStatus.TRIALING &&
                                sub.getTrialEndsAt() != null && sub.getTrialEndsAt().isBefore(now)) {
                            return false;
                        }
                        return true;
                    }

                    // If CANCELED, check if still within period end (grace period)
                    if (sub.getStatus() == SubscriptionStatus.CANCELED &&
                            sub.getCurrentPeriodEnd() != null &&
                            sub.getCurrentPeriodEnd().isAfter(now)) {
                        return true;
                    }

                    return false;
                })
                .orElse(false);
    }
}

