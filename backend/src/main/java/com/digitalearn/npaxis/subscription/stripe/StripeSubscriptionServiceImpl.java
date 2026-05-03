package com.digitalearn.npaxis.subscription.stripe;

import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.analytics.TrackEvent;
import com.digitalearn.npaxis.subscription.config.StripeProperties;
import com.digitalearn.npaxis.subscription.exceptions.StripeIntegrationException;
import com.digitalearn.npaxis.subscription.price.SubscriptionPrice;
import com.digitalearn.npaxis.subscription.price.SubscriptionPriceRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of StripeSubscriptionService.
 * Handles all Stripe Subscription API operations with proper error handling and transactional boundaries.
 *
 * ============================================
 * ANALYTICS TRACKING
 * ============================================
 * Tracks Stripe API operations:
 * - API_CALLED: stripe subscription API calls
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeSubscriptionServiceImpl implements StripeSubscriptionService {

    private final StripeProperties stripeProperties;
    private final StripeCustomerService stripeCustomerService;
    private final SubscriptionPriceRepository subscriptionPriceRepository;

    @Override
    @Transactional
    @TrackEvent(
        eventType = EventType.API_CALLED,
        targetIdExpression = "#userId.toString()",
        metadataExpression = "{'apiEndpoint': 'stripe.checkout.session.create', 'priceId': #priceId.toString(), 'status': 'success'}"
    )
    public String createCheckoutSession(Long userId, Long priceId) {
        log.info("Creating checkout session for userId: {}, priceId: {}", userId, priceId);

        try {
            // Get or create Stripe customer
            String stripeCustomerId = stripeCustomerService.getOrCreateCustomer(userId);

            // Retrieve the subscription price from database
            SubscriptionPrice subscriptionPrice = subscriptionPriceRepository.findById(priceId)
                    .orElseThrow(() -> new StripeIntegrationException(
                            "Subscription price not found: " + priceId
                    ));

            if (!subscriptionPrice.isActive()) {
                throw new StripeIntegrationException(
                        "Subscription price is not active: " + priceId
                );
            }

            // Build session creation params
            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(stripeProperties.getCurrency() != null ? stripeProperties.getCurrency() : "usd")
                    .setUnitAmount(subscriptionPrice.getAmountInMinorUnits())
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(subscriptionPrice.getPlan().getName())
                            .build())
                    .setRecurring(SessionCreateParams.LineItem.PriceData.Recurring.builder()
                            .setIntervalCount(1L)
                            .setInterval(getStripeInterval(subscriptionPrice.getBillingInterval()))
                            .build())
                    .build();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(priceData)
                    .setQuantity(1L)
                    .build();

            SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(stripeCustomerId)
                    .addLineItem(lineItem)
                    .setSuccessUrl(stripeProperties.getSuccessUrl())
                    .setCancelUrl(stripeProperties.getCancelUrl())
                    .putMetadata("preceptorId", userId.toString())
                    .putMetadata("priceId", priceId.toString());

            Session session = Session.create(sessionBuilder.build());
            log.info("Successfully created Stripe checkout session: {}", session.getId());
            return session.getUrl();

        } catch (StripeException e) {
            log.error("Stripe API error while creating checkout session for userId: {}", userId, e);
            throw new StripeIntegrationException(
                    "Failed to create checkout session: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    @Transactional
    @TrackEvent(
        eventType = EventType.API_CALLED,
        targetIdExpression = "#stripeCustomerId",
        metadataExpression = "{'apiEndpoint': 'stripe.billingportal.session.create', 'status': 'success'}"
    )
    public String createCustomerPortalSession(String stripeCustomerId) {
        log.info("Creating customer portal session for customer: {}", stripeCustomerId);

        try {
            com.stripe.param.billingportal.SessionCreateParams params = com.stripe.param.billingportal.SessionCreateParams.builder()
                    .setCustomer(stripeCustomerId)
                    .setReturnUrl(stripeProperties.getCustomerPortalReturnUrl())
                    .build();

            com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(params);
            log.info("Successfully created customer portal session");
            return session.getUrl();

        } catch (StripeException e) {
            log.error("Stripe API error while creating customer portal session", e);
            throw new StripeIntegrationException(
                    "Failed to create customer portal session: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Convert BillingInterval enum to Stripe interval
     */
    private SessionCreateParams.LineItem.PriceData.Recurring.Interval getStripeInterval(Object interval) {
        if (interval == null) {
            return SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH;
        }

        String intervalStr = interval.toString().toUpperCase();
        if ("YEARLY".equals(intervalStr) || "YEAR".equals(intervalStr)) {
            return SessionCreateParams.LineItem.PriceData.Recurring.Interval.YEAR;
        }
        return SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH;
    }
}






