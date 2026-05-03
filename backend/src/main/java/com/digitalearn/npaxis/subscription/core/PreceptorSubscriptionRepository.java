package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreceptorSubscriptionRepository extends BaseRepository<PreceptorSubscription, Long> {

    /**
     * Find the current active subscription for a preceptor
     * Returns the one and only active subscription (enforced at application level)
     */
    Optional<PreceptorSubscription> findByPreceptor_UserIdAndActiveTrue(Long preceptorId);

    /**
     * Legacy method for backward compatibility
     * Returns the most recent subscription (active or not) for a preceptor
     * Will be deprecated in favor of findByPreceptor_UserIdAndActiveTrue()
     *
     * @deprecated Use findByPreceptor_UserIdAndActiveTrue() instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    Optional<PreceptorSubscription> findByPreceptor_UserId(Long preceptorId);

    /**
     * Find all subscriptions for a preceptor ordered by creation date (newest first)
     * Useful for subscription history/timeline views
     */
    Page<PreceptorSubscription> findAllByPreceptor_UserIdOrderByCreatedAtDesc(Long preceptorId, Pageable pageable);

    /**
     * Find all active subscriptions for a preceptor
     * Should typically return 0 or 1 result (enforced at application level)
     */
    List<PreceptorSubscription> findAllByPreceptor_UserIdAndActiveTrue(Long preceptorId);

    Optional<PreceptorSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    @Deprecated(since = "1.0", forRemoval = true)
    Page<PreceptorSubscription> findByPreceptor_UserIdOrderByCreatedAtDesc(Long preceptorId, Pageable pageable);

    List<PreceptorSubscription> findByStatus(SubscriptionStatus status);

    List<PreceptorSubscription> findByCancelAtPeriodEndTrue();

    List<PreceptorSubscription> findByCurrentPeriodEndBefore(LocalDateTime dateTime);

    Optional<PreceptorSubscription> findByStripeCustomerId(String stripeCustomerId);

    /**
     * Idempotent UPSERT for subscription sync.
     * Uses database-level uniqueness to prevent duplicate stripe_subscription_id
     * <p>
     * UPSERT logic (PostgreSQL INSERT ... ON CONFLICT DO UPDATE):
     * - If stripe_subscription_id exists → UPDATE fields
     * - If stripe_subscription_id doesn't exist → INSERT new record
     * - If duplicate arrives concurrently → Database handles it atomically
     *
     * @param preceptorId          the preceptor ID
     * @param stripeSubscriptionId the Stripe subscription ID (unique)
     * @param stripePriceId        the Stripe price ID
     * @param status               the subscription status
     * @param cancelAtPeriodEnd    whether subscription cancels at period end
     * @param currentPeriodStart   start of current billing period
     * @param currentPeriodEnd     end of current billing period
     * @param nextBillingDate      next billing date
     */
    @Modifying
    @Query(value = """
            INSERT INTO preceptor_subscriptions
                (preceptor_id, stripe_subscription_id, stripe_price_id, status, cancel_at_period_end, 
                 current_period_start, current_period_end, next_billing_date, created_at, last_modified_at, created_by, last_modified_by)
            VALUES 
                (:preceptorId, :stripeSubscriptionId, :stripePriceId, :status, :cancelAtPeriodEnd,
                 :currentPeriodStart, :currentPeriodEnd, :nextBillingDate, NOW(), NOW(), 'system', 'system')
            ON CONFLICT (stripe_subscription_id) DO UPDATE SET
                preceptor_id = EXCLUDED.preceptor_id,
                stripe_price_id = EXCLUDED.stripe_price_id,
                status = EXCLUDED.status,
                cancel_at_period_end = EXCLUDED.cancel_at_period_end,
                current_period_start = EXCLUDED.current_period_start,
                current_period_end = EXCLUDED.current_period_end,
                next_billing_date = EXCLUDED.next_billing_date,
                last_modified_at = NOW(),
                last_modified_by = 'system'
            """, nativeQuery = true)
    void upsertSubscription(
            @Param("preceptorId") Long preceptorId,
            @Param("stripeSubscriptionId") String stripeSubscriptionId,
            @Param("stripePriceId") String stripePriceId,
            @Param("status") String status,
            @Param("cancelAtPeriodEnd") Boolean cancelAtPeriodEnd,
            @Param("currentPeriodStart") LocalDateTime currentPeriodStart,
            @Param("currentPeriodEnd") LocalDateTime currentPeriodEnd,
            @Param("nextBillingDate") LocalDateTime nextBillingDate
    );

    long countByStatusAndDeletedFalse(SubscriptionStatus status);

    long countByStatusInAndDeletedFalse(List<SubscriptionStatus> statuses);

    /**
     * Deactivate all subscriptions for a preceptor except the specified one
     * Used when activating a new subscription to mark prior active ones as inactive
     *
     * @param preceptorId           the preceptor ID
     * @param excludeSubscriptionId the subscription ID to keep active (can be null to deactivate all)
     * @return number of subscriptions updated
     */
    @Modifying
    @Query("UPDATE PreceptorSubscription ps SET ps.active = false, ps.endDate = CURRENT_TIMESTAMP " +
            "WHERE ps.preceptor.userId = :preceptorId " +
            "AND ps.active = true " +
            "AND (:excludeId IS NULL OR ps.preceptorSubscriptionId != :excludeId)")
    int deactivateOtherSubscriptions(@Param("preceptorId") Long preceptorId, @Param("excludeId") Long excludeSubscriptionId);

}