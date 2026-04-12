package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.subscription.config.StripeProperties;
import com.digitalearn.npaxis.subscription.price.SubscriptionPrice;
import com.digitalearn.npaxis.subscription.price.SubscriptionPriceRepository;
import com.digitalearn.npaxis.subscription.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPriceRepository priceRepo;
    private final PreceptorRepository preceptorRepo;
    private final PreceptorSubscriptionRepository subscriptionRepo;
    private final SubscriptionMapper subscriptionMapper;
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;

    @Override
    public CreateCheckoutSessionResponse createCheckoutSession(Long preceptorId, Long priceId) {

        Preceptor preceptor = preceptorRepo.findById(preceptorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Preceptor not found with ID: " + preceptorId));

        SubscriptionPrice price = priceRepo.findById(priceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription Plan Price not found with ID: " + priceId));

        try {
            // Create customer if not exists
            if (preceptor.getStripeCustomerId() == null) {

                Customer customer = stripeClient.createCustomer(preceptor.getEmail());
                preceptor.setStripeCustomerId(customer.getId());
                preceptorRepo.save(preceptor);
            }

            // Build checkout session params
            com.stripe.param.checkout.SessionCreateParams params =
                    com.stripe.param.checkout.SessionCreateParams.builder()
                            .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                            .setCustomer(preceptor.getStripeCustomerId())
                            .setSuccessUrl(stripeProperties.getSuccessUrl())
                            .setCancelUrl(stripeProperties.getCancelUrl())
                            .addLineItem(
                                    com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                                            .setPrice(price.getStripePriceId())
                                            .setQuantity(1L)
                                            .build()
                            )
                            .build();

            Session session = stripeClient.createCheckoutSession(params);

            return new CreateCheckoutSessionResponse(
                    session.getId(),
                    session.getUrl()
            );

        } catch (StripeException e) {
            log.error("Stripe error while creating checkout session", e);
            throw new RuntimeException("Stripe error", e);
        }
    }

    @Override
    public SubscriptionStatusResponse getSubscription(Long preceptorId) {
        return subscriptionRepo.findByPreceptor_UserId(preceptorId)
                .map(subscriptionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found for Preceptor with ID: " + preceptorId));
    }

    @Override
    public void cancelSubscription(Long preceptorId) {

        PreceptorSubscription sub = subscriptionRepo.findByPreceptor_UserId(preceptorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found for Preceptor with ID: " + preceptorId));

        try {
            stripeClient.cancelSubscription(sub.getStripeSubscriptionId());

        } catch (StripeException e) {
            log.error("Stripe error while cancelling subscription", e);
            throw new RuntimeException("Stripe error", e);
        }
    }

    @Override
    public String createCustomerPortal(Long preceptorId) {

        Preceptor preceptor = preceptorRepo.findById(preceptorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Preceptor not found with ID: " + preceptorId));

        try {
            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(preceptor.getStripeCustomerId())
                            .setReturnUrl(stripeProperties.getCustomerPortalReturnUrl())
                            .build();

            return stripeClient.createCustomerPortal(params).getUrl();

        } catch (StripeException e) {
            log.error("Stripe error while creating customer portal", e);
            throw new RuntimeException("Stripe error", e);
        }
    }
}