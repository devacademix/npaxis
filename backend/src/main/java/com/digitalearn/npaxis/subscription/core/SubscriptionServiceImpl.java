package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.analytics.TrackEvent;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.exceptions.SubscriptionException;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.subscription.config.StripeProperties;
import com.digitalearn.npaxis.subscription.price.SubscriptionPrice;
import com.digitalearn.npaxis.subscription.price.SubscriptionPriceRepository;
import com.digitalearn.npaxis.subscription.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Service implementation for managing preceptor subscriptions.
 * <p>
 * Handles checkout, cancellation, updates, and premium access validation.
 * <p>
 * ============================================
 * ANALYTICS TRACKING
 * ============================================
 * <p>
 * This service is instrumented with @TrackEvent annotations to automatically
 * capture subscription lifecycle events:
 * <p>
 * - SUBSCRIPTION_PAGE_VIEWED: when preceptor views checkout page
 * - SUBSCRIPTION_CANCELED: when a preceptor cancels their subscription
 * - SUBSCRIPTION_UPGRADED/SUBSCRIPTION_DOWNGRADED: when plan is changed via updateSubscription()
 * <p>
 * Events are tracked asynchronously without blocking subscription operations.
 * Metadata includes plan details and amounts for business analysis.
 *
 * @see TrackEvent
 * @see EventType
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final PreceptorSubscriptionRepository subscriptionRepository;
    private final SubscriptionPriceRepository priceRepository;
    private final PreceptorRepository preceptorRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final SubscriptionEmailService subscriptionEmailService;
    private final EntityManager entityManager;
    private final SubscriptionRetryService retryService;
    private final SubscriptionEventService eventService;

    @Override
    @TrackEvent(
            eventType = EventType.SUBSCRIPTION_PAGE_VIEWED,
            targetIdExpression = "#userId.toString()",
            metadataExpression = "{'priceId': #priceId.toString()}"
    )
    public CreateCheckoutSessionResponse createCheckoutSession(Long userId, Long priceId) {
        log.info("Creating checkout session for user: {}, price: {}", userId, priceId);

        try {
            Preceptor preceptor = preceptorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found"));

            SubscriptionPrice price = priceRepository.findById(priceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

            String stripeCustomerId = preceptor.getStripeCustomerId();
            if (stripeCustomerId == null) {
                Customer customer = stripeClient.createCustomer(preceptor.getUser().getEmail());
                stripeCustomerId = customer.getId();
                preceptor.setStripeCustomerId(stripeCustomerId);
                preceptorRepository.save(preceptor);
                log.info("Created new Stripe customer: {} for preceptor: {}", stripeCustomerId, userId);
            }

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(stripeCustomerId)
                    .setSuccessUrl(stripeProperties.getSuccessUrl())
                    .setCancelUrl(stripeProperties.getCancelUrl())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(price.getStripePriceId())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            Session session = stripeClient.createCheckoutSession(params);
            log.info("Checkout session created successfully: {}", session.getId());

            return new CreateCheckoutSessionResponse(
                    session.getId(),
                    session.getUrl(),
                    session.getCustomer()
            );

        } catch (StripeException e) {
            log.error("Stripe error while creating checkout session", e);
            throw new SubscriptionException("Failed to create checkout session: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDetailResponse getSubscriptionDetail(Long userId) {
        log.info("Fetching active subscription details for user: {}", userId);

        PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        return subscriptionMapper.toDetailResponse(subscription);
    }

    /**
     * Cancels an active subscription.
     * <p>
     * ANALYTICS:
     * - Tracks SUBSCRIPTION_CANCELED event when cancellation is successful
     * - Metadata includes plan code and cancellation date
     */
    @Override
    @TrackEvent(
            eventType = EventType.SUBSCRIPTION_CANCELED,
            metadataExpression = "{'planCode': #subscription.getPlan().getCode(), " +
                    "'cancelDate': #subscription.getCancelDate(), " +
                    "'accessRetainedUntil': #subscription.getEndDate()}"
    )
    public void cancelSubscription(Long userId) {
        log.info("Canceling active subscription for user: {}", userId);

        try {
            PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserIdAndActiveTrue(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

            stripeClient.cancelSubscription(subscription.getStripeSubscriptionId());

            subscription.setCancelAtPeriodEnd(true);
            subscription.setCancelDate(subscription.getCurrentPeriodEnd());
            subscription.setCanceledAt(LocalDateTime.now());
            subscription.setCanceledReason("User requested cancellation");
            subscription.setCancelled(true);
            subscription.setActive(false); // Mark as inactive but retain for history
            subscription.setEndDate(subscription.getCurrentPeriodEnd()); // Retain access until end of period
            subscription.setNextBillingDate(null);
            subscriptionRepository.save(subscription);

            log.info("Subscription canceled for user: {}, access retained until: {}", userId, subscription.getCurrentPeriodEnd());

            // Log the cancellation event
            eventService.logSubscriptionCancelled(subscription, "User requested cancellation");

            subscriptionEmailService.sendSubscriptionCanceledEmail(subscription);

        } catch (StripeException e) {
            log.error("Stripe error while canceling subscription", e);
            throw new SubscriptionException("Failed to cancel subscription: " + e.getMessage());
        }
    }

    /**
     * Updates a subscription to a different plan/price.
     * <p>
     * ANALYTICS:
     * - Tracks SUBSCRIPTION_UPGRADED event when plan is successfully changed
     * - Metadata includes price information for analysis
     */
    @Override
    @TrackEvent(
            eventType = EventType.SUBSCRIPTION_UPGRADED,
            metadataExpression = "{'priceId': #request.priceId()}"
    )
    public void updateSubscription(Long userId, UpdateSubscriptionRequest request) {
        log.info("Updating active subscription for user: {} with new price: {}", userId, request.priceId());

        try {
            PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserIdAndActiveTrue(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

            SubscriptionPrice newPrice = priceRepository.findById(request.priceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

            // Store old plan info for event logging
            Long previousPlanId = subscription.getPlan().getSubscriptionPlanId();
            String previousPlanCode = subscription.getPlan().getCode();

            PreceptorSubscription previousSubscription = PreceptorSubscription.builder()
                    .preceptor(subscription.getPreceptor())
                    .plan(subscription.getPlan())
                    .price(subscription.getPrice())
                    .status(subscription.getStatus())
                    .build();

            stripeClient.updateSubscription(
                    subscription.getStripeSubscriptionId(),
                    newPrice.getStripePriceId()
            );

            subscription.setPrice(newPrice);
            subscription.setStartDate(LocalDateTime.now()); // Update start date for the price change
            subscriptionRepository.save(subscription);

            log.info("Subscription plan changed successfully for user: {}, from {} to {}", userId, previousPlanCode, subscription.getPlan().getCode());

            // Log the appropriate event based on plan comparison
            logPlanChangeEvent(subscription, previousPlanId, previousPlanCode);

            subscriptionEmailService.sendSubscriptionUpgradedEmail(subscription, previousSubscription);

        } catch (StripeException e) {
            log.error("Stripe error while updating subscription", e);
            throw new SubscriptionException("Failed to update subscription: " + e.getMessage());
        }
    }

    /**
     * Helper method to log the appropriate plan change event
     */
    private void logPlanChangeEvent(PreceptorSubscription subscription, Long previousPlanId, String previousPlanCode) {
        Long newPlanId = subscription.getPlan().getSubscriptionPlanId();

        if (newPlanId.equals(previousPlanId)) {
            // No plan change, might be price/interval change
            log.debug("Price updated but plan unchanged for subscription: {}", subscription.getPreceptorSubscriptionId());
            return;
        }

        // Determine if upgrade or downgrade (could also be lateral move)
        // This is simplified - in a production system, you might have plan tiers
        eventService.logPlanChanged(subscription, previousPlanId, previousPlanCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionHistoryResponse> getSubscriptionHistory(Long userId, Pageable pageable) {
        log.info("Fetching subscription history for user: {}", userId);

        Page<PreceptorSubscription> history = subscriptionRepository
                .findAllByPreceptor_UserIdOrderByCreatedAtDesc(userId, pageable);

        return history.map(subscriptionMapper::toHistoryResponse);
    }

    @Override
    public String createCustomerPortal(Long userId) {
        log.info("Creating customer portal for user: {}", userId);

        try {
            Preceptor preceptor = preceptorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found"));

            subscriptionRepository.findByPreceptor_UserIdAndActiveTrue(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

            if (preceptor.getStripeCustomerId() == null) {
                throw new SubscriptionException("Stripe customer not found");
            }

            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(preceptor.getStripeCustomerId())
                            .setReturnUrl(stripeProperties.getCustomerPortalReturnUrl())
                            .build();

            com.stripe.model.billingportal.Session session = stripeClient.createCustomerPortal(params);
            log.info("Customer portal created successfully for user: {}", userId);

            return session.getUrl();

        } catch (StripeException e) {
            log.error("Stripe error while creating customer portal", e);
            throw new SubscriptionException("Failed to create customer portal: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessPremiumFeatures(Long userId) {
        log.debug("Checking premium access for user: {}", userId);

        Optional<PreceptorSubscription> subscription = subscriptionRepository.findByPreceptor_UserIdAndActiveTrue(userId);

        if (subscription.isEmpty()) {
            return false;
        }

        PreceptorSubscription sub = subscription.get();
        LocalDateTime now = LocalDateTime.now();

        // Active subscription with good standing
        if (sub.getStatus() == SubscriptionStatus.ACTIVE || sub.getStatus() == SubscriptionStatus.TRIALING) {
            return true;
        }

        // Allow past_due with grace period through current period end
        if (sub.getStatus() == SubscriptionStatus.PAST_DUE &&
                sub.getCurrentPeriodEnd() != null &&
                sub.getCurrentPeriodEnd().isAfter(now)) {
            return true;
        }

        return false;
    }

    @Override
    public void syncLocalSubscriptionFromStripe(String stripeSubscriptionId) {
        log.info("Syncing subscription from Stripe: {}", stripeSubscriptionId);

        try {
            Subscription stripeSubscription = stripeClient.retrieveSubscription(stripeSubscriptionId);

            SubscriptionItem firstItem = stripeSubscription.getItems() != null
                    && stripeSubscription.getItems().getData() != null
                    && !stripeSubscription.getItems().getData().isEmpty()
                    ? stripeSubscription.getItems().getData().getFirst()
                    : null;

            if (firstItem == null || firstItem.getPrice() == null) {
                throw new SubscriptionException("Invalid Stripe subscription: missing price");
            }

            String customerId = stripeSubscription.getCustomer();

            Preceptor preceptor = preceptorRepository.findByStripeCustomerId(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Preceptor not found for customer: " + customerId));

            String stripePriceId = firstItem.getPrice().getId();

            Optional<PreceptorSubscription> existing =
                    subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);

            boolean isNew = existing.isEmpty();

            LocalDateTime periodStart = toLocalDateTime(firstItem.getCurrentPeriodStart());
            LocalDateTime periodEnd = toLocalDateTime(firstItem.getCurrentPeriodEnd());

            SubscriptionStatus status = mapStripeStatusToLocal(stripeSubscription.getStatus());

            // Determine if this subscription should be considered "active"
            // (i.e., can grant access to premium features)
            boolean shouldBeActive = status == SubscriptionStatus.ACTIVE
                    || status == SubscriptionStatus.TRIALING
                    || (status == SubscriptionStatus.CANCELED
                    && periodEnd != null
                    && periodEnd.isAfter(LocalDateTime.now()));

            boolean accessEnabled = shouldBeActive;

            // Determine if subscription is cancelled
            boolean isCancelled = status == SubscriptionStatus.CANCELED;
            LocalDateTime nextBillingDate = isCancelled ? null : periodEnd;

            upsertSubscription(
                    preceptor.getUserId(),
                    stripeSubscriptionId,
                    stripePriceId,
                    status,
                    stripeSubscription.getCancelAtPeriodEnd(),
                    periodStart,
                    periodEnd,
                    nextBillingDate,
                    isCancelled,
                    shouldBeActive
            );

            PreceptorSubscription refreshed = subscriptionRepository
                    .findByStripeSubscriptionId(stripeSubscriptionId)
                    .orElseThrow();

            refreshed.setStripeCustomerId(customerId);
            refreshed.setAccessEnabled(accessEnabled);
            refreshed.setStartDate(periodStart);
            refreshed.setActive(shouldBeActive);

            // If this is a new subscription and should be active, deactivate old active subscriptions
            if (shouldBeActive) {
                subscriptionRepository.deactivateOtherSubscriptions(preceptor.getUserId(), refreshed.getPreceptorSubscriptionId());
                refreshed.setActive(true); // Ensure this one is marked active
            }

            subscriptionRepository.save(refreshed);

            preceptor.setStripeCustomerId(customerId);
            preceptor.setStripeSubscriptionId(stripeSubscriptionId);
            preceptor.setPremium(accessEnabled);
            preceptorRepository.save(preceptor);

            if (isNew) {
                log.info("New subscription synced from Stripe for preceptor: {}", preceptor.getUserId());
                // Log the creation event with Stripe event ID if available
                eventService.logSubscriptionCreated(refreshed, stripeSubscription.getObject());
                subscriptionEmailService.sendSubscriptionCreatedEmail(refreshed);
            } else {
                log.info("Existing subscription updated from Stripe: {}", stripeSubscriptionId);
                // Log status change if status changed
                Optional<SubscriptionEvent> latestStatusChangeEvent = eventService.getLatestEventOfType(
                        refreshed.getPreceptorSubscriptionId(),
                        SubscriptionEventType.STATUS_CHANGED
                );
                // Only log if status actually changed (optimize to avoid duplicate events)
                if (latestStatusChangeEvent.isEmpty() || !latestStatusChangeEvent.get().getDetails()
                        .get("new_status").equals(status.toString())) {
                    eventService.logStatusChanged(refreshed, status, "Synced from Stripe", stripeSubscription.getId());
                }
            }

        } catch (Exception e) {
            log.error("Error syncing subscription", e);
            throw new SubscriptionException("Failed to sync subscription: " + e.getMessage());
        }
    }


    private void upsertSubscription(
            Long preceptorId,
            String stripeSubscriptionId,
            String stripePriceId,
            SubscriptionStatus status,
            Boolean cancelAtPeriodEnd,
            LocalDateTime currentPeriodStart,
            LocalDateTime currentPeriodEnd,
            LocalDateTime nextBillingDate,
            boolean cancelled,
            boolean isActive
    ) {
        // Always run in a fresh transaction so any prior constraint violations
        // in the outer transaction don't poison this operation.
        retryService.upsertInNewTransaction(
                preceptorId,
                stripeSubscriptionId,
                stripePriceId,
                status,
                cancelAtPeriodEnd,
                currentPeriodStart,
                currentPeriodEnd,
                nextBillingDate,
                cancelled,
                isActive
        );
    }

    private LocalDateTime toLocalDateTime(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(epochSeconds),
                ZoneId.systemDefault()
        );
    }

    private SubscriptionStatus mapStripeStatusToLocal(String stripeStatus) {
        if (stripeStatus == null) {
            throw new IllegalStateException("Stripe status is null");
        }

        return switch (stripeStatus) {
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "active" -> SubscriptionStatus.ACTIVE;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            case "unpaid" -> SubscriptionStatus.UNPAID;
            default -> {
                log.warn("Unknown Stripe status: {}", stripeStatus);
                yield SubscriptionStatus.INCOMPLETE;
            }
        };
    }
}
