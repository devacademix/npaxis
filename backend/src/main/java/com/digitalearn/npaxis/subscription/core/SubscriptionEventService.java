package com.digitalearn.npaxis.subscription.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing subscription event audit trail
 * Logs all subscription lifecycle changes for auditing and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionEventService {

    private final SubscriptionEventRepository eventRepository;

    /**
     * Log that a subscription was created
     */
    public SubscriptionEvent logSubscriptionCreated(
            PreceptorSubscription subscription,
            String stripeEventId
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("plan_id", subscription.getPlan().getSubscriptionPlanId());
        details.put("plan_code", subscription.getPlan().getCode());
        details.put("plan_name", subscription.getPlan().getName());
        details.put("status", subscription.getStatus().toString());
        details.put("start_date", subscription.getStartDate());
        details.put("trial_ends_at", subscription.getTrialEndsAt());

        return createEvent(
                subscription,
                SubscriptionEventType.CREATED,
                stripeEventId,
                details
        );
    }

    /**
     * Log that a subscription was activated (e.g., trial ended, payment succeeded)
     */
    public SubscriptionEvent logSubscriptionActivated(
            PreceptorSubscription subscription,
            String stripeEventId,
            String reason
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("status", subscription.getStatus().toString());
        details.put("reason", reason);
        details.put("activated_at", LocalDateTime.now());

        return createEvent(
                subscription,
                SubscriptionEventType.ACTIVATED,
                stripeEventId,
                details
        );
    }

    /**
     * Log that a subscription was cancelled by the user
     */
    public SubscriptionEvent logSubscriptionCancelled(
            PreceptorSubscription subscription,
            String cancellationReason
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("status", subscription.getStatus().toString());
        details.put("reason", cancellationReason);
        details.put("cancelled_at", subscription.getCanceledAt());
        details.put("current_period_end", subscription.getCurrentPeriodEnd());
        details.put("cancel_at_period_end", subscription.isCancelAtPeriodEnd());

        return createEvent(
                subscription,
                SubscriptionEventType.CANCELLED,
                null,
                details
        );
    }

    /**
     * Log that a subscription expired (period ended without renewal)
     */
    public SubscriptionEvent logSubscriptionExpired(
            PreceptorSubscription subscription
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("status", subscription.getStatus().toString());
        details.put("end_date", subscription.getEndDate());
        details.put("last_period_end", subscription.getCurrentPeriodEnd());

        return createEvent(
                subscription,
                SubscriptionEventType.EXPIRED,
                null,
                details
        );
    }

    /**
     * Log that a user reactivated their subscription after cancellation
     */
    public SubscriptionEvent logSubscriptionReactivated(
            PreceptorSubscription subscription,
            PreceptorSubscription previousSubscription
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("plan_id", subscription.getPlan().getSubscriptionPlanId());
        details.put("new_status", subscription.getStatus().toString());
        details.put("reactivated_at", subscription.getStartDate());
        if (previousSubscription != null) {
            details.put("previous_subscription_id", previousSubscription.getPreceptorSubscriptionId());
            details.put("previous_status", previousSubscription.getStatus().toString());
        }

        return createEvent(
                subscription,
                SubscriptionEventType.REACTIVATED,
                null,
                details
        );
    }

    /**
     * Log that a subscription plan was upgraded
     */
    public SubscriptionEvent logPlanUpgraded(
            PreceptorSubscription subscription,
            SubscriptionStatus previousStatus,
            Long previousPlanId,
            String previousPlanCode
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("new_plan_id", subscription.getPlan().getSubscriptionPlanId());
        details.put("new_plan_code", subscription.getPlan().getCode());
        details.put("new_plan_name", subscription.getPlan().getName());
        details.put("previous_plan_id", previousPlanId);
        details.put("previous_plan_code", previousPlanCode);
        details.put("upgrade_date", LocalDateTime.now());

        return createEvent(
                subscription,
                SubscriptionEventType.PLAN_UPGRADED,
                null,
                details
        );
    }

    /**
     * Log that a subscription plan was downgraded
     */
    public SubscriptionEvent logPlanDowngraded(
            PreceptorSubscription subscription,
            Long previousPlanId,
            String previousPlanCode
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("new_plan_id", subscription.getPlan().getSubscriptionPlanId());
        details.put("new_plan_code", subscription.getPlan().getCode());
        details.put("new_plan_name", subscription.getPlan().getName());
        details.put("previous_plan_id", previousPlanId);
        details.put("previous_plan_code", previousPlanCode);
        details.put("downgrade_date", LocalDateTime.now());

        return createEvent(
                subscription,
                SubscriptionEventType.PLAN_DOWNGRADED,
                null,
                details
        );
    }

    /**
     * Log that a subscription plan was changed (lateral move)
     */
    public SubscriptionEvent logPlanChanged(
            PreceptorSubscription subscription,
            Long previousPlanId,
            String previousPlanCode
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("new_plan_id", subscription.getPlan().getSubscriptionPlanId());
        details.put("new_plan_code", subscription.getPlan().getCode());
        details.put("new_plan_name", subscription.getPlan().getName());
        details.put("previous_plan_id", previousPlanId);
        details.put("previous_plan_code", previousPlanCode);
        details.put("change_date", LocalDateTime.now());

        return createEvent(
                subscription,
                SubscriptionEventType.PLAN_CHANGED,
                null,
                details
        );
    }

    /**
     * Log that a subscription's billing interval was changed
     */
    public SubscriptionEvent logBillingIntervalChanged(
            PreceptorSubscription subscription,
            String previousInterval,
            String newInterval
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("previous_interval", previousInterval);
        details.put("new_interval", newInterval);
        details.put("change_date", LocalDateTime.now());

        return createEvent(
                subscription,
                SubscriptionEventType.BILLING_INTERVAL_CHANGED,
                null,
                details
        );
    }

    /**
     * Log that a payment failed
     */
    public SubscriptionEvent logPaymentFailed(
            PreceptorSubscription subscription,
            String failureReason,
            String stripeEventId
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("status", subscription.getStatus().toString());
        details.put("failure_reason", failureReason);
        details.put("last_failure_reason", subscription.getLastPaymentFailureReason());
        details.put("retry_count", subscription.getPaymentRetryCount());

        return createEvent(
                subscription,
                SubscriptionEventType.PAYMENT_FAILED,
                stripeEventId,
                details
        );
    }

    /**
     * Log that a payment succeeded
     */
    public SubscriptionEvent logPaymentSucceeded(
            PreceptorSubscription subscription,
            String stripeEventId
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("status", subscription.getStatus().toString());
        details.put("current_period_end", subscription.getCurrentPeriodEnd());
        details.put("next_billing_date", subscription.getNextBillingDate());

        return createEvent(
                subscription,
                SubscriptionEventType.PAYMENT_SUCCEEDED,
                stripeEventId,
                details
        );
    }

    /**
     * Log a generic status change
     */
    public SubscriptionEvent logStatusChanged(
            PreceptorSubscription subscription,
            SubscriptionStatus previousStatus,
            String reason,
            String stripeEventId
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("previous_status", previousStatus.toString());
        details.put("new_status", subscription.getStatus().toString());
        details.put("reason", reason);
        details.put("change_date", LocalDateTime.now());

        return createEvent(
                subscription,
                SubscriptionEventType.STATUS_CHANGED,
                stripeEventId,
                details
        );
    }

    /**
     * Log that subscription marked as past due
     */
    public SubscriptionEvent logPastDue(
            PreceptorSubscription subscription,
            String stripeEventId
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("status", subscription.getStatus().toString());
        details.put("current_period_end", subscription.getCurrentPeriodEnd());
        details.put("marked_at", LocalDateTime.now());

        return createEvent(
                subscription,
                SubscriptionEventType.PAST_DUE,
                stripeEventId,
                details
        );
    }

    /**
     * Log that subscription marked as unpaid
     */
    public SubscriptionEvent logUnpaid(
            PreceptorSubscription subscription,
            String stripeEventId
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("status", subscription.getStatus().toString());
        details.put("marked_at", LocalDateTime.now());

        return createEvent(
                subscription,
                SubscriptionEventType.UNPAID,
                stripeEventId,
                details
        );
    }

    /**
     * Log a system error during subscription processing
     */
    public SubscriptionEvent logError(
            PreceptorSubscription subscription,
            SubscriptionEventType relatedEvent,
            String errorMessage,
            String stripeEventId
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("related_event", relatedEvent.toString());
        details.put("error_message", errorMessage);
        details.put("status", subscription.getStatus().toString());

        SubscriptionEvent event = createEvent(
                subscription,
                SubscriptionEventType.ERROR_OCCURRED,
                stripeEventId,
                details
        );
        event.setStatus(SubscriptionEventStatus.FAILED);
        event.setErrorMessage(errorMessage);

        return eventRepository.save(event);
    }

    /**
     * Get event history for a subscription (paginated)
     */
    @Transactional(readOnly = true)
    public Page<SubscriptionEvent> getSubscriptionEventHistory(Long subscriptionId, Pageable pageable) {
        return eventRepository.findBySubscription_PreceptorSubscriptionIdOrderByCreatedAtDesc(
                subscriptionId,
                pageable
        );
    }

    /**
     * Get event history for a preceptor (all subscriptions, paginated)
     */
    @Transactional(readOnly = true)
    public Page<SubscriptionEvent> getPreceptorEventHistory(Long preceptorId, Pageable pageable) {
        return eventRepository.findByPreceptorIdOrderByCreatedAtDesc(preceptorId, pageable);
    }

    /**
     * Get specific type of events for a preceptor
     */
    @Transactional(readOnly = true)
    public List<SubscriptionEvent> getPreceptorEventsByType(Long preceptorId, SubscriptionEventType eventType) {
        return eventRepository.findByPreceptorIdAndEventTypeOrderByCreatedAtDesc(preceptorId, eventType);
    }

    /**
     * Check if a Stripe event was already processed (idempotency)
     */
    @Transactional(readOnly = true)
    public boolean isStripeEventProcessed(String stripeEventId) {
        if (stripeEventId == null) {
            return false;
        }
        return eventRepository.findByStripeEventId(stripeEventId).isPresent();
    }

    /**
     * Get the most recent event of a specific type for a subscription
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionEvent> getLatestEventOfType(
            Long subscriptionId,
            SubscriptionEventType eventType
    ) {
        return eventRepository.findFirstBySubscription_PreceptorSubscriptionIdAndEventTypeOrderByCreatedAtDesc(
                subscriptionId,
                eventType
        );
    }

    // ========================
    // Private helper methods
    // ========================

    /**
     * Core method to create and save an event
     */
    private SubscriptionEvent createEvent(
            PreceptorSubscription subscription,
            SubscriptionEventType eventType,
            String stripeEventId,
            Map<String, Object> details
    ) {
        SubscriptionEvent event = SubscriptionEvent.builder()
                .subscription(subscription)
                .preceptorId(subscription.getPreceptor().getUserId())
                .eventType(eventType)
                .stripeEventId(stripeEventId)
                .details(details)
                .status(SubscriptionEventStatus.SUCCESS)
                .build();

        SubscriptionEvent savedEvent = eventRepository.save(event);
        log.info("Subscription event logged: type={}, subscriptionId={}, preceptorId={}, stripeEventId={}",
                eventType, subscription.getPreceptorSubscriptionId(),
                subscription.getPreceptor().getUserId(), stripeEventId);

        return savedEvent;
    }
}





