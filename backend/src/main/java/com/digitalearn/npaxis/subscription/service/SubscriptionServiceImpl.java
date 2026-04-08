package com.digitalearn.npaxis.subscription.service;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.subscription.dto.CreateSubscriptionRequest;
import com.digitalearn.npaxis.subscription.dto.SubscriptionResponse;
import com.digitalearn.npaxis.subscription.entity.*;
import com.digitalearn.npaxis.subscription.repository.*;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
    private final PlanPriceRepository planPriceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final StripeService stripeService;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PlanPrice planPrice = planPriceRepository.findByStripePriceId(request.priceId())
                .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

        if (!planPrice.isActive()) {
            throw new BusinessException(BusinessErrorCodes.BAD_REQUEST);
        }

        if (user.getStripeCustomerId() == null) {
            com.stripe.model.Customer stripeCustomer = stripeService.createCustomer(user);
            user.setStripeCustomerId(stripeCustomer.getId());
            userRepository.save(user);
        }

        com.stripe.model.Subscription stripeSub = stripeService.createSubscription(
                user.getStripeCustomerId(),
                planPrice.getStripePriceId()
        );

        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .user(user)
                .stripeSubscriptionId(stripeSub.getId())
                .planPrice(planPrice)
                .status(SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase()))
                .currentPeriodStart(toLocalDateTime(stripeSub.getCurrentPeriodStart()))
                .currentPeriodEnd(toLocalDateTime(stripeSub.getCurrentPeriodEnd()))
                .cancelAtPeriodEnd(stripeSub.getCancelAtPeriodEnd())
                .build();

        subscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    @Override
    @Transactional
    public void cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SubscriptionEntity subscription = subscriptionRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        stripeService.cancelSubscription(subscription.getStripeSubscriptionId());

        subscription.setCancelAtPeriodEnd(true);
        subscriptionRepository.save(subscription);
    }

    @Override
    public SubscriptionResponse getSubscriptionByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SubscriptionEntity subscription = subscriptionRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new ResourceNotFoundException("No subscription found"));

        return mapToResponse(subscription);
    }

    @Override
    @Transactional
    public void processWebhook(com.stripe.model.Event event) {
        log.info("Processing Stripe event: {}", event.getType());

        event.getDataObjectDeserializer().getObject().ifPresent(stripeObject -> {
            if (stripeObject instanceof com.stripe.model.Invoice) {
                com.stripe.model.Invoice stripeInvoice = (com.stripe.model.Invoice) stripeObject;
                switch (event.getType()) {
                    case "invoice.payment_succeeded" -> handleInvoicePaymentSucceeded(stripeInvoice);
                    case "invoice.payment_failed" -> handleInvoicePaymentFailed(stripeInvoice);
                    case "invoice.created", "invoice.finalized" -> handleInvoiceCreated(stripeInvoice);
                }
            } else if (stripeObject instanceof com.stripe.model.Subscription) {
                com.stripe.model.Subscription stripeSub = (com.stripe.model.Subscription) stripeObject;
                switch (event.getType()) {
                    case "customer.subscription.created" -> handleSubscriptionCreated(stripeSub);
                    case "customer.subscription.updated" -> handleSubscriptionUpdated(stripeSub);
                    case "customer.subscription.deleted" -> handleSubscriptionDeleted(stripeSub);
                }
            }
        });
    }

    private void handleInvoicePaymentSucceeded(com.stripe.model.Invoice stripeInvoice) {
        String subId = stripeInvoice.getSubscription();
        if (subId == null) return;

        subscriptionRepository.findByStripeSubscriptionId(subId).ifPresent(s -> {
            s.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(s);

            User user = s.getUser();
            PaymentEntity p = PaymentEntity.builder()
                    .user(user)
                    .stripePaymentIntentId(stripeInvoice.getPaymentIntent())
                    .stripeInvoiceId(stripeInvoice.getId())
                    .amount(BigDecimal.valueOf(stripeInvoice.getAmountPaid()).divide(BigDecimal.valueOf(100)))
                    .currency(stripeInvoice.getCurrency())
                    .status(PaymentStatus.SUCCEEDED)
                    .build();
            paymentRepository.save(p);
        });
    }

    private void handleInvoicePaymentFailed(com.stripe.model.Invoice stripeInvoice) {
        String subId = stripeInvoice.getSubscription();
        if (subId == null) return;

        subscriptionRepository.findByStripeSubscriptionId(subId).ifPresent(s -> {
            s.setStatus(SubscriptionStatus.PAST_DUE);
            subscriptionRepository.save(s);
        });
    }

    private void handleSubscriptionCreated(com.stripe.model.Subscription stripeSub) {
        if (subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId()).isPresent()) {
            return;
        }

        String customerId = stripeSub.getCustomer();
        User user = userRepository.findByStripeCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for customer: " + customerId));

        String priceId = stripeSub.getItems().getData().get(0).getPrice().getId();
        PlanPrice planPrice = planPriceRepository.findByStripePriceId(priceId)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found: " + priceId));

        SubscriptionEntity s = SubscriptionEntity.builder()
                .user(user)
                .stripeSubscriptionId(stripeSub.getId())
                .planPrice(planPrice)
                .status(SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase()))
                .currentPeriodStart(toLocalDateTime(stripeSub.getCurrentPeriodStart()))
                .currentPeriodEnd(toLocalDateTime(stripeSub.getCurrentPeriodEnd()))
                .cancelAtPeriodEnd(stripeSub.getCancelAtPeriodEnd())
                .build();

        subscriptionRepository.save(s);
    }

    private void handleSubscriptionUpdated(com.stripe.model.Subscription stripeSub) {
        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId()).ifPresent(s -> {
            s.setStatus(SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase()));
            s.setCurrentPeriodStart(toLocalDateTime(stripeSub.getCurrentPeriodStart()));
            s.setCurrentPeriodEnd(toLocalDateTime(stripeSub.getCurrentPeriodEnd()));
            s.setCancelAtPeriodEnd(stripeSub.getCancelAtPeriodEnd());
            
            String priceId = stripeSub.getItems().getData().get(0).getPrice().getId();
            if (!s.getPlanPrice().getStripePriceId().equals(priceId)) {
                planPriceRepository.findByStripePriceId(priceId).ifPresent(s::setPlanPrice);
            }
            
            subscriptionRepository.save(s);
        });
    }

    private void handleSubscriptionDeleted(com.stripe.model.Subscription stripeSub) {
        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId()).ifPresent(s -> {
            s.setStatus(SubscriptionStatus.CANCELED);
            subscriptionRepository.save(s);
        });
    }

    private void handleInvoiceCreated(com.stripe.model.Invoice stripeInvoice) {
        if (invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId()).isPresent()) {
            return;
        }

        SubscriptionEntity sub = null;
        if (stripeInvoice.getSubscription() != null) {
            sub = subscriptionRepository.findByStripeSubscriptionId(stripeInvoice.getSubscription()).orElse(null);
        }

        InvoiceEntity inv = InvoiceEntity.builder()
                .stripeInvoiceId(stripeInvoice.getId())
                .subscription(sub)
                .amountDue(BigDecimal.valueOf(stripeInvoice.getAmountDue()).divide(BigDecimal.valueOf(100)))
                .amountPaid(BigDecimal.valueOf(stripeInvoice.getAmountPaid()).divide(BigDecimal.valueOf(100)))
                .status(stripeInvoice.getStatus())
                .invoicePdf(stripeInvoice.getInvoicePdf())
                .hostedInvoiceUrl(stripeInvoice.getHostedInvoiceUrl())
                .build();

        invoiceRepository.save(inv);
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }

    private SubscriptionResponse mapToResponse(SubscriptionEntity subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getUserId(),
                subscription.getStripeSubscriptionId(),
                subscription.getPlanPrice().getPlan().getName(),
                subscription.getPlanPrice().getStripePriceId(),
                subscription.getStatus(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.isCancelAtPeriodEnd()
        );
    }
}
