package com.digitalearn.npaxis.subscription.core;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Service implementation for managing preceptor subscriptions
 * Handles checkout, cancellation, updates, and premium access validation
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

    @Override
    public CreateCheckoutSessionResponse createCheckoutSession(Long userId, Long priceId) {
        log.info("Creating checkout session for user: {}, price: {}", userId, priceId);

        try {
            Preceptor preceptor = preceptorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found"));

            SubscriptionPrice price = priceRepository.findById(priceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

            String stripeCustomerId = preceptor.getStripeCustomerId();
            if (stripeCustomerId == null) {
                Customer customer = stripeClient.createCustomer(preceptor.getEmail());
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
        log.info("Fetching subscription details for user: {}", userId);

        PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        return subscriptionMapper.toDetailResponse(subscription);
    }

    @Override
    public void cancelSubscription(Long userId) {
        log.info("Canceling subscription for user: {}", userId);

        try {
            PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

            stripeClient.cancelSubscription(subscription.getStripeSubscriptionId());

            subscription.setCancelAtPeriodEnd(true);
            subscription.setCanceledAt(LocalDateTime.now());
            subscription.setCanceledReason("User requested cancellation");
            subscriptionRepository.save(subscription);

            log.info("Subscription canceled at period end for user: {}", userId);

            subscriptionEmailService.sendSubscriptionCanceledEmail(subscription);

        } catch (StripeException e) {
            log.error("Stripe error while canceling subscription", e);
            throw new SubscriptionException("Failed to cancel subscription: " + e.getMessage());
        }
    }

    @Override
    public void updateSubscription(Long userId, UpdateSubscriptionRequest request) {
        log.info("Updating subscription for user: {} with price: {}", userId, request.priceId());

        try {
            PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

            SubscriptionPrice newPrice = priceRepository.findById(request.priceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

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
            subscriptionRepository.save(subscription);

            log.info("Subscription updated successfully for user: {}", userId);

            subscriptionEmailService.sendSubscriptionUpgradedEmail(subscription, previousSubscription);

        } catch (StripeException e) {
            log.error("Stripe error while updating subscription", e);
            throw new SubscriptionException("Failed to update subscription: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionHistoryResponse> getSubscriptionHistory(Long userId, Pageable pageable) {
        log.info("Fetching subscription history for user: {}", userId);

        Page<PreceptorSubscription> history = subscriptionRepository
                .findByPreceptor_UserIdOrderByCreatedAtDesc(userId, pageable);

        return history.map(subscriptionMapper::toHistoryResponse);
    }

    @Override
    public String createCustomerPortal(Long userId) {
        log.info("Creating customer portal for user: {}", userId);

        try {
            Preceptor preceptor = preceptorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found"));

            subscriptionRepository.findByPreceptor_UserId(userId)
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

        Optional<PreceptorSubscription> subscription = subscriptionRepository.findByPreceptor_UserId(userId);

        if (subscription.isEmpty()) {
            return false;
        }

        PreceptorSubscription sub = subscription.get();
        LocalDateTime now = LocalDateTime.now();

        if (sub.getStatus() == SubscriptionStatus.ACTIVE || sub.getStatus() == SubscriptionStatus.TRIALING) {
            return true;
        }

        if (sub.getStatus() == SubscriptionStatus.CANCELED &&
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

            upsertSubscription(
                    preceptor.getUserId(),
                    stripeSubscriptionId,
                    stripePriceId,
                    status,
                    stripeSubscription.getCancelAtPeriodEnd(),
                    periodStart,
                    periodEnd,
                    periodEnd
            );

            if (isNew) {
                PreceptorSubscription refreshed = subscriptionRepository
                        .findByStripeSubscriptionId(stripeSubscriptionId)
                        .orElseThrow();

                subscriptionEmailService.sendSubscriptionCreatedEmail(refreshed);
            }

        } catch (Exception e) {
            log.error("Error syncing subscription", e);
            throw new SubscriptionException("Failed to sync subscription: " + e.getMessage());
        }
    }

// In SubscriptionServiceImpl.java — replace upsertSubscription entirely

    private void upsertSubscription(
            Long preceptorId,
            String stripeSubscriptionId,
            String stripePriceId,
            SubscriptionStatus status,
            Boolean cancelAtPeriodEnd,
            LocalDateTime currentPeriodStart,
            LocalDateTime currentPeriodEnd,
            LocalDateTime nextBillingDate
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
                nextBillingDate
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