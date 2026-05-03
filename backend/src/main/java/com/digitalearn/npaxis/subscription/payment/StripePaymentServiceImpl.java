package com.digitalearn.npaxis.subscription.payment;

import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.analytics.TrackEvent;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.subscription.BillingCycle;
import com.digitalearn.npaxis.subscription.CheckoutSessionRequest;
import com.digitalearn.npaxis.subscription.CheckoutSessionResponse;
import com.digitalearn.npaxis.subscription.StripeConfigProperties;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentServiceImpl implements PaymentGatewayService {

    private final StripeConfigProperties stripeConfig;
    private final PreceptorRepository preceptorRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeConfig.getApiKey();
        log.info("Stripe SDK Initialized.");
    }

    @Override
    @TrackEvent(
            eventType = EventType.SUBSCRIPTION_PAGE_VIEWED,
            targetIdExpression = "#request.preceptorId().toString()",
            metadataExpression = "{'billingCycle': #request.billingCycle().name(), 'isExistingCustomer': #preceptor.getStripeCustomerId() != null}"
    )
    public CheckoutSessionResponse createCheckoutSession(CheckoutSessionRequest request) {

        Preceptor preceptor = preceptorRepository.findById(request.preceptorId())
                .orElseThrow(() -> new IllegalArgumentException("Preceptor not found"));

        if (preceptor.isPremium()) {
            throw new IllegalStateException("Preceptor is already premium.");
        }

        String selectedPriceId = resolvePriceId(request.billingCycle());

        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(request.successUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(request.cancelUrl())
                    .putMetadata("preceptorId", preceptor.getUserId().toString())
                    .putMetadata("billingCycle", request.billingCycle().name())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPrice(selectedPriceId)
                                    .build()
                    );

            if (preceptor.getStripeCustomerId() != null) {
                paramsBuilder.setCustomer(preceptor.getStripeCustomerId());
            } else {
                paramsBuilder.setCustomerEmail(preceptor.getUser().getEmail());
            }

            Session session = Session.create(paramsBuilder.build());

            log.info("Created Stripe Checkout Session {} (Plan: {}) for Preceptor {}",
                    session.getId(), request.billingCycle(), preceptor.getUserId());

            return new CheckoutSessionResponse(session.getId(), session.getUrl());

        } catch (StripeException e) {
            log.error("Failed to create Stripe Checkout Session", e);
            // We throw a generic domain exception so the controller doesn't need to know about StripeException
            throw new RuntimeException("Payment service unavailable. Please try again later.");
        }
    }

    private String resolvePriceId(BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> stripeConfig.getPrices().getPremiumMonthly();
            case YEARLY -> stripeConfig.getPrices().getPremiumYearly();
            default -> throw new IllegalArgumentException("Unsupported billing cycle");
        };
    }
}