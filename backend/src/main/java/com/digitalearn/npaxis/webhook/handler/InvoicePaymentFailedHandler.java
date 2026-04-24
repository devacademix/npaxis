package com.digitalearn.npaxis.webhook.handler;

import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoice;
import com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoiceRepository;
import com.digitalearn.npaxis.subscription.billing.invoice.InvoiceStatus;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransaction;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransactionRepository;
import com.digitalearn.npaxis.subscription.billing.transaction.TransactionStatus;
import com.digitalearn.npaxis.subscription.core.PreceptorSubscriptionRepository;
import com.digitalearn.npaxis.subscription.core.SubscriptionService;
import com.digitalearn.npaxis.webhook.WebhookProcessingEvent;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Webhook handler for invoice.payment_failed events.
 * Records failed payment transactions and stores failure reason on subscription.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentFailedHandler implements WebhookEventHandler {

    private final PreceptorRepository preceptorRepository;
    private final PreceptorSubscriptionRepository subscriptionRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final BillingTransactionRepository billingTransactionRepository;
    private final SubscriptionService subscriptionService;

    @Override
    public String eventType() {
        return "invoice.payment_failed";
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Event event, WebhookProcessingEvent webhookRecord) {
        log.info("Handling invoice.payment_failed event: {}", event.getId());

        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Invoice invoice = (Invoice) deserializer.deserializeUnsafe();

            if (invoice == null) {
                throw new IllegalStateException("Failed to deserialize Invoice object from event");
            }

            // Find preceptor by customer ID
            Preceptor preceptor = preceptorRepository.findByStripeCustomerId(invoice.getCustomer())
                    .orElseThrow(() -> new IllegalStateException("Preceptor not found for customer: " + invoice.getCustomer()));

            // Extract subscription ID from raw JSON
            String stripeSubscriptionId = extractSubscriptionId(invoice);

            // Save billing invoice
            saveBillingInvoice(invoice, preceptor, stripeSubscriptionId);

            // Record failed transaction using payment intent ID
            String paymentIntentId = extractPaymentIntentId(invoice);
            if (paymentIntentId != null) {
                saveBillingTransaction(preceptor, invoice, paymentIntentId, stripeSubscriptionId);
            }

            // Store failure reason on subscription if available
            if (stripeSubscriptionId != null) {
                updateSubscriptionFailureInfo(invoice, stripeSubscriptionId);
                subscriptionService.syncLocalSubscriptionFromStripe(stripeSubscriptionId);
            }

            webhookRecord.markSucceeded();
            log.info("Invoice payment failed recorded: {}", invoice.getId());

        } catch (Exception e) {
            log.error("Error handling invoice.payment_failed: {}", event.getId(), e);
            webhookRecord.markFailed(e.getMessage());
            webhookRecord.incrementRetry();
        }
    }

    /**
     * Save billing invoice from failed payment
     */
    private void saveBillingInvoice(Invoice invoice, Preceptor preceptor, String stripeSubscriptionId) {
        BillingInvoice billingInvoice = billingInvoiceRepository
                .findByStripeInvoiceId(invoice.getId())
                .orElse(BillingInvoice.builder()
                        .stripeInvoiceId(invoice.getId())
                        .preceptor(preceptor)
                        .build());

        billingInvoice.setStripeCustomerId(invoice.getCustomer());
        billingInvoice.setStripeSubscriptionId(stripeSubscriptionId);
        billingInvoice.setAmountPaidInMinorUnits(invoice.getAmountPaid() != null ? invoice.getAmountPaid() : 0L);
        billingInvoice.setAmountDueInMinorUnits(invoice.getAmountDue() != null ? invoice.getAmountDue() : 0L);
        billingInvoice.setCurrency(invoice.getCurrency());
        billingInvoice.setStatus(InvoiceStatus.OPEN); // Payment failed, not voided
        billingInvoice.setHostedInvoiceUrl(invoice.getHostedInvoiceUrl());
        billingInvoice.setInvoicePdfUrl(invoice.getInvoicePdf());
        billingInvoice.setInvoiceCreatedAt(toLocalDateTime(invoice.getCreated()));

        billingInvoiceRepository.save(billingInvoice);
    }

    /**
     * Save failed transaction
     */
    private void saveBillingTransaction(Preceptor preceptor, Invoice invoice, String paymentIntentId, String stripeSubscriptionId) {
        BillingTransaction transaction = billingTransactionRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .orElse(BillingTransaction.builder()
                        .preceptor(preceptor)
                        .build());

        transaction.setStripePaymentIntentId(paymentIntentId);
        transaction.setStripeInvoiceId(invoice.getId());
        transaction.setStripeSubscriptionId(stripeSubscriptionId);
        transaction.setAmountInMinorUnits(invoice.getAmountDue() != null ? invoice.getAmountDue() : 0L);
        transaction.setCurrency(invoice.getCurrency());
        transaction.setStatus(TransactionStatus.FAILED);
        String failureReason = invoice.getLastFinalizationError() != null
                ? invoice.getLastFinalizationError().getMessage()
                : "Payment failed - reason unknown";
        transaction.setFailureReason(failureReason);
        transaction.setTransactionAt(toLocalDateTime(invoice.getCreated()));

        billingTransactionRepository.save(transaction);
    }

    /**
     * Update subscription with failure information
     */
    private void updateSubscriptionFailureInfo(Invoice invoice, String stripeSubscriptionId) {
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .ifPresent(sub -> {
                    String failureReason = invoice.getLastFinalizationError() != null
                            ? invoice.getLastFinalizationError().getMessage()
                            : "Payment failed - reason unknown";
                    sub.setLastPaymentFailureReason(failureReason);
                    sub.setPaymentRetryCount((sub.getPaymentRetryCount() != null ? sub.getPaymentRetryCount() : 0) + 1);
                    subscriptionRepository.save(sub);
                });
    }

    /**
     * Extract subscription ID from invoice using raw JSON
     */
    private String extractSubscriptionId(Invoice invoice) {
        try {
            Object rawJson = invoice.getRawJsonObject();
            if (rawJson != null) {
                Object subValue = rawJson.getClass().getMethod("get", String.class)
                        .invoke(rawJson, "subscription");
                return extractStringValue(subValue);
            }
        } catch (Exception e) {
            log.debug("Could not extract subscription ID from invoice: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract payment intent ID from invoice using raw JSON
     */
    private String extractPaymentIntentId(Invoice invoice) {
        try {
            Object rawJson = invoice.getRawJsonObject();
            if (rawJson != null) {
                Object piValue = rawJson.getClass().getMethod("get", String.class)
                        .invoke(rawJson, "payment_intent");
                return extractStringValue(piValue);
            }
        } catch (Exception e) {
            log.debug("Could not extract payment intent ID from invoice: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract string value from Stripe object field
     */
    private String extractStringValue(Object value) {
        if (value == null) {
            return null;
        }

        String stringValue = value.toString().trim();

        if (stringValue.isEmpty() || "null".equalsIgnoreCase(stringValue) ||
                stringValue.equals("JsonNull") || stringValue.equals("{}")) {
            return null;
        }

        // Remove surrounding quotes if present
        if (stringValue.startsWith("\"") && stringValue.endsWith("\"")) {
            stringValue = stringValue.substring(1, stringValue.length() - 1).trim();
        }

        return stringValue.isEmpty() ? null : stringValue;
    }

    private LocalDateTime toLocalDateTime(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(epochSeconds),
                ZoneId.of("UTC")
        );
    }
}




