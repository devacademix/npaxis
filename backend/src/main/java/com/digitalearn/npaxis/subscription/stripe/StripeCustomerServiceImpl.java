package com.digitalearn.npaxis.subscription.stripe;

import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.analytics.TrackEvent;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.subscription.exceptions.StripeIntegrationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of StripeCustomerService.
 * Handles all Stripe Customer API operations with proper error handling and transactional boundaries.
 *
 * ============================================
 * ANALYTICS TRACKING
 * ============================================
 * Tracks Stripe customer operations:
 * - API_CALLED: stripe customer creation and updates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeCustomerServiceImpl implements StripeCustomerService {

    private final PreceptorRepository preceptorRepository;

    @Override
    @Transactional
    @TrackEvent(
        eventType = EventType.API_CALLED,
        targetIdExpression = "#userId.toString()",
        metadataExpression = "{'apiEndpoint': 'stripe.customer.create', 'email': #email, 'status': 'success'}"
    )
    public Customer createCustomer(Long userId, String name, String email) {
        log.info("Creating Stripe Customer for userId: {}, email: {}", userId, email);

        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .setDescription("Preceptor account | userId: " + userId)
                    .build();

            Customer customer = Customer.create(params);
            log.info("Successfully created Stripe Customer: {}", customer.getId());
            return customer;

        } catch (StripeException e) {
            log.error("Stripe API error while creating customer for userId: {}", userId, e);
            throw new StripeIntegrationException(
                    "Failed to create Stripe Customer: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    @Transactional
    @TrackEvent(
        eventType = EventType.API_CALLED,
        targetIdExpression = "#userId.toString()",
        metadataExpression = "{'apiEndpoint': 'stripe.customer.get_or_create', 'isExisting': #preceptor.getStripeCustomerId() != null}"
    )
    public String getOrCreateCustomer(Long userId) {
        log.info("Getting or creating Stripe Customer for userId: {}", userId);

        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new StripeIntegrationException(
                        "Preceptor not found for userId: " + userId
                ));

        // If customer ID already exists, return it
        if (preceptor.getStripeCustomerId() != null && !preceptor.getStripeCustomerId().isBlank()) {
            log.debug("Preceptor already has Stripe Customer ID: {}", preceptor.getStripeCustomerId());
            return preceptor.getStripeCustomerId();
        }

        // Create new customer
        Customer customer = createCustomer(userId, preceptor.getUser().getName(), preceptor.getUser().getEmail());

        // Persist Stripe customer ID to preceptor
        preceptor.setStripeCustomerId(customer.getId());
        preceptorRepository.save(preceptor);
        log.info("Persisted Stripe Customer ID to preceptor: {}", customer.getId());

        return customer.getId();
    }
}



