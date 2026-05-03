package com.digitalearn.npaxis.webhook;


import com.digitalearn.npaxis.analytics.AnalyticsService;
import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoiceRepository;
import com.digitalearn.npaxis.subscription.billing.invoice.InvoiceStatus;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransaction;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransactionRepository;
import com.digitalearn.npaxis.subscription.billing.transaction.TransactionStatus;
import com.digitalearn.npaxis.subscription.core.SubscriptionEmailService;
import com.digitalearn.npaxis.subscription.core.SubscriptionService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service implementation for processing Stripe webhook events
 * Handles subscription lifecycle, invoices, and payment events
 * Saves all transactions and updates preceptor premium status
 *
 * ============================================
 * ANALYTICS TRACKING
 * ============================================
 * This service tracks all critical payment events:
 * - PAYMENT_SUCCEEDED: invoice/payment successful
 * - PAYMENT_FAILED: invoice/payment failed
 * - SUBSCRIPTION_* events: subscription lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookServiceImpl implements WebhookService {

    // Maximum retries for failed webhook events
    private static final int MAX_RETRY_COUNT = 3;
    // Initial retry delay in minutes
    private static final int INITIAL_RETRY_DELAY_MINUTES = 5;

    // Common log messages
    private static final String SUB_NO_CUSTOMER_MSG = "Subscription {} has no customer ID, skipping preceptor lookup";
    private static final String PRECEPTOR_NOT_FOUND_MSG = "Preceptor not found for customer ID: {}";
    private static final String ERROR_SYNC_SUBSCRIPTION_MSG = "Error syncing subscription from stripe: {}";
    private static final String INVOICE_NO_CUSTOMER_MSG = "Invoice has no customer ID, cannot process";

    private final WebhookProcessingEventRepository webhookEventRepository;
    private final SubscriptionService subscriptionService;
    private final WebhookEventMapper webhookEventMapper;
    private final PreceptorRepository preceptorRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final BillingTransactionRepository billingTransactionRepository;
    private final SubscriptionEmailService subscriptionEmailService;
    private final AnalyticsService analyticsService;

    @Override
    public void process(Event event, String payload) {
        log.info("Processing Stripe webhook event: {} - {}", event.getId(), event.getType());

        // Check for idempotency - ensure this event hasn't been processed
        if (webhookEventRepository.existsByEventId(event.getId())) {
            log.info("Event already processed, skipping: {}", event.getId());
            return;
        }

        // Create webhook processing event record
        WebhookProcessingEvent webhookEvent = WebhookProcessingEvent.builder()
                .eventId(event.getId())
                .eventType(event.getType())
                .payload(payload)
                .status(WebhookEventStatus.PENDING)
                .retryCount(0)
                .liveMode(event.getLivemode())
                .build();

        try {
            // Process based on event type
            processEventByType(event, webhookEvent);

            // Mark as successfully processed
            webhookEvent.setStatus(WebhookEventStatus.SUCCESS);
            webhookEvent.setProcessedAt(LocalDateTime.now());
            log.info("Event processed successfully: {}", event.getId());

        } catch (Exception e) {
            log.error("Error processing webhook event: {}", event.getId(), e);

            webhookEvent.setStatus(WebhookEventStatus.FAILED_RETRYING);
            webhookEvent.setErrorMessage(e.getMessage());
            webhookEvent.setRetryCount(0);

            // Schedule for retry
            webhookEvent.setNextRetryAt(
                    LocalDateTime.now().plusMinutes(INITIAL_RETRY_DELAY_MINUTES)
            );
        }

        // Always persist the webhook event
        webhookEventRepository.save(webhookEvent);
    }

    @Override
    public void retryFailedEvents() {
        log.info("Retrying failed webhook events");

        LocalDateTime now = LocalDateTime.now();
        List<WebhookProcessingEvent> failedEvents = webhookEventRepository
                .findByStatusAndNextRetryAtBefore(WebhookEventStatus.FAILED_RETRYING, now);

        log.info("Found {} failed events to retry", failedEvents.size());

        for (WebhookProcessingEvent event : failedEvents) {
            if (event.getRetryCount() >= MAX_RETRY_COUNT) {
                log.warn("Event {} exceeded max retries, giving up", event.getEventId());
                continue;
            }

            try {
                log.info("Retrying webhook event: {} (attempt {}/{})",
                        event.getEventId(), event.getRetryCount() + 1, MAX_RETRY_COUNT);

                // Re-process the event
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Event stripeEvent = mapper.readValue(event.getPayload(), Event.class);
                processEventByType(stripeEvent, event);

                // Mark as successfully processed
                event.setStatus(WebhookEventStatus.SUCCESS);
                event.setProcessedAt(LocalDateTime.now());
                webhookEventRepository.save(event);

                log.info("Webhook event successfully retried: {}", event.getEventId());

            } catch (Exception e) {
                log.error("Error retrying webhook event: {}", event.getEventId(), e);

                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());

                // Schedule next retry with exponential backoff
                int delayMinutes = INITIAL_RETRY_DELAY_MINUTES * (event.getRetryCount() + 1);
                event.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));

                webhookEventRepository.save(event);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookEventResponse> getEventHistory(Pageable pageable) {
        log.info("Fetching webhook event history");

        Page<WebhookProcessingEvent> events = webhookEventRepository
                .findAllByOrderByCreatedAtDesc(pageable);

        return events.map(webhookEventMapper::toResponse);
    }

    /**
     * Route event processing based on event type
     */
    private void processEventByType(Event event, WebhookProcessingEvent webhookEvent) {
        String eventType = event.getType();

        switch (eventType) {
            case "customer.subscription.created":
                handleSubscriptionCreated(event, webhookEvent);
                break;

            case "customer.subscription.updated":
                handleSubscriptionUpdated(event, webhookEvent);
                break;

            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event, webhookEvent);
                break;

            case "invoice.payment_succeeded":
                handleInvoicePaymentSucceeded(event, webhookEvent);
                break;

            case "invoice.payment_failed":
                handleInvoicePaymentFailed(event, webhookEvent);
                break;

            case "invoice.created":
                handleInvoiceCreated(event, webhookEvent);
                break;

            case "invoice.paid":
                handleInvoicePaid(event, webhookEvent);
                break;

            case "invoice.finalized":
                handleInvoiceFinalized(event, webhookEvent);
                break;

            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event, webhookEvent);
                break;

            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event, webhookEvent);
                break;

            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event, webhookEvent);
                break;

            case "charge.succeeded":
                handleChargeSucceeded(event, webhookEvent);
                break;

            case "payment_method.attached":
                handlePaymentMethodAttached(event, webhookEvent);
                break;

            case "customer.updated":
                handleCustomerUpdated(event, webhookEvent);
                break;

            case "payment_intent.created":
                handlePaymentIntentCreated(event, webhookEvent);
                break;

            case "invoice_payment.paid":
                handleInvoicePaymentPaid(event, webhookEvent);
                break;

            default:
                log.warn("Unhandled webhook event type: {}", eventType);
        }
    }

    /**
     * Handle subscription.created event
     */
    private void handleSubscriptionCreated(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing subscription created event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Subscription subscription = (Subscription) deserializer.getObject().get();

            // Store customer ID in webhook event for tracking
            String customerId = subscription.getCustomer();
            String stripeSubscriptionId = subscription.getId();
            webhookEvent.setStripeCustomerId(customerId);

            log.debug("Subscription created: id={}, customer={}, status={}",
                    stripeSubscriptionId, customerId, subscription.getStatus());

            if (customerId == null) {
                log.warn(SUB_NO_CUSTOMER_MSG, stripeSubscriptionId);
            } else {
                // Find and update preceptor
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);
                    log.info("Found preceptor {} for subscription created event", p.getUserId());
                } else {
                    log.warn(PRECEPTOR_NOT_FOUND_MSG, customerId);
                }
            }

            // Sync local subscription from Stripe to populate PreceptorSubscription table
            try {
                subscriptionService.syncLocalSubscriptionFromStripe(stripeSubscriptionId);
                log.info("Subscription created and synced: {}", stripeSubscriptionId);
            } catch (Exception e) {
                log.error(ERROR_SYNC_SUBSCRIPTION_MSG, stripeSubscriptionId, e);
                throw new RuntimeException("Failed to sync subscription", e);
            }
        }
    }

    /**
     * Handle subscription.updated event
     */
    private void handleSubscriptionUpdated(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing subscription updated event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Subscription subscription = (Subscription) deserializer.getObject().get();

            // Store customer ID in webhook event
            String customerId = subscription.getCustomer();
            String stripeSubscriptionId = subscription.getId();
            webhookEvent.setStripeCustomerId(customerId);

            log.debug("Subscription updated: id={}, customer={}, status={}",
                    stripeSubscriptionId, customerId, subscription.getStatus());

            if (customerId == null) {
                log.warn(SUB_NO_CUSTOMER_MSG, stripeSubscriptionId);
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);
                    log.info("Found preceptor {} for subscription updated event", p.getUserId());
                    // Premium status will be set when invoice.paid is received
                } else {
                    log.warn(PRECEPTOR_NOT_FOUND_MSG, customerId);
                }
            }

            try {
                subscriptionService.syncLocalSubscriptionFromStripe(stripeSubscriptionId);
                log.info("Subscription updated and synced: {}", stripeSubscriptionId);
            } catch (Exception e) {
                log.error(ERROR_SYNC_SUBSCRIPTION_MSG, stripeSubscriptionId, e);
                throw new RuntimeException("Failed to sync subscription", e);
            }
        }
    }

    /**
     * Handle subscription.deleted event
     */
    private void handleSubscriptionDeleted(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing subscription deleted event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Subscription subscription = (Subscription) deserializer.getObject().get();

            // Store customer ID in webhook event
            String customerId = subscription.getCustomer();
            String stripeSubscriptionId = subscription.getId();
            webhookEvent.setStripeCustomerId(customerId);

            log.debug("Subscription deleted: id={}, customer={}, status={}",
                    stripeSubscriptionId, customerId, subscription.getStatus());

            if (customerId == null) {
                log.warn(SUB_NO_CUSTOMER_MSG, stripeSubscriptionId);
            } else {
                // Find and update preceptor - remove premium on delete
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);

                    p.setPremium(false);
                    preceptorRepository.save(p);
                    log.info("Removed premium from preceptor {} on subscription deletion", p.getUserId());
                } else {
                    log.warn(PRECEPTOR_NOT_FOUND_MSG, customerId);
                }
            }

            try {
                subscriptionService.syncLocalSubscriptionFromStripe(stripeSubscriptionId);
                log.info("Subscription deleted and synced: {}", stripeSubscriptionId);
            } catch (Exception e) {
                log.error(ERROR_SYNC_SUBSCRIPTION_MSG, stripeSubscriptionId, e);
                throw new RuntimeException("Failed to sync subscription", e);
            }
        }
    }

    /**
     * Handle invoice.payment_succeeded event - Save transaction & send email with PDF attachment
     * AUTHORITATIVE EVENT for invoice persistence (uses UPSERT)
     * Safe for concurrent webhook delivery (idempotent via ON CONFLICT DO UPDATE)
     * Multiple deliveries of same event result in single invoice record
     * Sends email with invoice PDF as attachment using invoice-payment.html template
     */
    private void handleInvoicePaymentSucceeded(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing invoice payment succeeded event (AUTHORITATIVE)");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String customerId = invoice.getCustomer();
            webhookEvent.setStripeCustomerId(customerId);

            if (customerId == null) {
                log.warn(INVOICE_NO_CUSTOMER_MSG);
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);

                    // Save transaction
                    saveBillingTransaction(p, invoice, TransactionStatus.SUCCEEDED);

                    // Track analytics event BEFORE email operations
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("invoiceId", invoice.getId());
                    metadata.put("amount", invoice.getAmountPaid());
                    metadata.put("currency", invoice.getCurrency() != null ? invoice.getCurrency() : "usd");
                    metadata.put("paymentStatus", "succeeded");
                    analyticsService.trackBackendEvent(
                        EventType.PAYMENT_SUCCEEDED,
                        p.getUserId(),
                        p.getUserId().toString(),
                        metadata
                    );

                    // Extract invoice data for PDF generation and storage
                    String invoiceNumber = invoice.getNumber() != null ? invoice.getNumber() : invoice.getId();
                    String hostedInvoiceUrl = invoice.getHostedInvoiceUrl() != null ? invoice.getHostedInvoiceUrl() : "";
                    Long amountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : 0L;
                    String currency = invoice.getCurrency() != null ? invoice.getCurrency() : "usd";
                    Long invoiceCreatedAt = invoice.getCreated();
                    LocalDateTime invoiceCreatedAtDt = invoiceCreatedAt != null ?
                            LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(invoiceCreatedAt), ZoneId.systemDefault())
                            : LocalDateTime.now();

                    // Generate, store PDF persistently, and get storage URL
                    String invoicePdfUrl = subscriptionEmailService.generateAndStoreInvoicePdf(
                            invoiceNumber,
                            p.getUser().getName(),
                            invoiceCreatedAtDt,
                            amountPaid,
                            currency,
                            hostedInvoiceUrl
                    );

                    // UPSERT invoice WITH stored PDF URL (PostgreSQL ON CONFLICT DO UPDATE)
                    // Safe for concurrent webhook events
                    updateOrCreateBillingInvoice(p, invoice, InvoiceStatus.PAID, invoicePdfUrl);

                    // Send async email WITH PDF attachment
                    subscriptionEmailService.sendInvoicePaymentEmailWithPdf(
                            p.getUserId(),
                            p.getUser().getName(),
                            p.getUser().getEmail(),
                            invoiceNumber,
                            amountPaid,
                            currency,
                            invoiceCreatedAtDt,
                            hostedInvoiceUrl
                    );

                    log.info("✓ Invoice payment succeeded (UPSERT + EMAIL WITH PDF): preceptor={}, invoiceId={}, status=PAID, pdfUrl={}",
                            p.getUserId(), invoice.getId(), invoicePdfUrl);
                } else {
                    log.warn(PRECEPTOR_NOT_FOUND_MSG, customerId);
                }
            }
        }
    }

    /**
     * Handle invoice.payment_failed event - Save failed transaction and track analytics
     */
    private void handleInvoicePaymentFailed(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing invoice payment failed event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String customerId = invoice.getCustomer();
            webhookEvent.setStripeCustomerId(customerId);

            // Find preceptor by customer ID
            Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
            if (preceptor.isPresent()) {
                Preceptor p = preceptor.get();
                webhookEvent.setPreceptor(p);

                // Save failed transaction
                saveBillingTransaction(p, invoice, TransactionStatus.FAILED);

                // Track analytics event
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("invoiceId", invoice.getId());
                metadata.put("amount", invoice.getAmountDue());
                metadata.put("currency", invoice.getCurrency() != null ? invoice.getCurrency() : "usd");
                metadata.put("paymentStatus", "failed");
                metadata.put("failureReason", "Payment failed - check Stripe dashboard for details");
                analyticsService.trackBackendEvent(
                    EventType.PAYMENT_FAILED,
                    p.getUserId(),
                    p.getUserId().toString(),
                    metadata
                );

                // Update invoice record as open (still needs payment)
                updateOrCreateBillingInvoice(p, invoice, InvoiceStatus.OPEN);

                log.warn("Invoice payment failed for preceptor: {}", p.getUserId());
            }
        }
    }

    /**
     * Handle invoice.created event
     * NOTE: This is NOT authoritative for invoice persistence
     * Stripe may send this multiple times with no changes
     * Only log for visibility - actual persistence done via invoice.payment_succeeded or invoice.paid
     */
    private void handleInvoiceCreated(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing invoice created event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String customerId = invoice.getCustomer();
            webhookEvent.setStripeCustomerId(customerId);

            if (customerId == null) {
                log.warn(INVOICE_NO_CUSTOMER_MSG);
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);

                    // NOTE: Do NOT persist invoice on creation - only log for monitoring
                    // The invoice will be persisted when invoice.payment_succeeded or invoice.paid is received
                    log.info("Invoice created (not persisting): invoiceId={}, preceptor={}, amount={} {}",
                            invoice.getId(), p.getUserId(), invoice.getAmountDue(), invoice.getCurrency());
                } else {
                    log.warn(PRECEPTOR_NOT_FOUND_MSG, customerId);
                }
            }
        }
    }

    /**
     * Handle invoice.paid event - Mark invoice as paid, set premium status, send email with PDF
     * AUTHORITATIVE EVENT for invoice persistence (uses UPSERT)
     * Safe for concurrent webhook delivery (idempotent via ON CONFLICT DO UPDATE)
     * Sets preceptor to premium status on payment confirmation
     * Sends email with invoice PDF as attachment using invoice-payment.html template
     */
    private void handleInvoicePaid(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing invoice paid event (AUTHORITATIVE)");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String customerId = invoice.getCustomer();
            webhookEvent.setStripeCustomerId(customerId);

            if (customerId == null) {
                log.warn("Invoice has no customer ID, skipping preceptor premium update");
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);

                    // Set preceptor to premium on successful invoice payment
                    p.setPremium(true);
                    preceptorRepository.save(p);
                    log.info("✓ Set preceptor {} to premium (payment confirmed)", p.getUserId());

                    // Save transaction
                    saveBillingTransaction(p, invoice, TransactionStatus.SUCCEEDED);

                    // NOTE: PDF generation and email are handled by handleInvoicePaymentSucceeded event
                    // Do NOT generate PDF here to avoid duplicate PDFs in storage
                    // invoice.paid fires AFTER invoice.payment_succeeded, so payment_succeeded is authoritative

                    // Just update invoice status to PAID
                    updateOrCreateBillingInvoice(p, invoice, InvoiceStatus.PAID);

                    log.info("✓ Invoice marked as paid (UPSERT only): preceptor={}, invoiceId={}, status=PAID",
                            p.getUserId(), invoice.getId());
                } else {
                    log.warn("Preceptor not found for customer ID: {}. Premium status not set.", customerId);
                }
            }
        }
    }

    /**
     * Handle invoice.finalized event
     * NOTE: This is NOT authoritative for invoice persistence
     * Only indicates invoice is ready for payment
     * Actual persistence done via invoice.payment_succeeded or invoice.paid
     * Log for visibility only
     */
    private void handleInvoiceFinalized(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing invoice finalized event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String customerId = invoice.getCustomer();
            webhookEvent.setStripeCustomerId(customerId);

            if (customerId == null) {
                log.warn(INVOICE_NO_CUSTOMER_MSG);
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);

                    // NOTE: Do NOT persist invoice on finalization
                    // Only log for monitoring - invoice will be persisted when payment received
                    log.info("Invoice finalized (not persisting): invoiceId={}, preceptor={}, status=READY_FOR_PAYMENT",
                            invoice.getId(), p.getUserId());
                } else {
                    log.warn(PRECEPTOR_NOT_FOUND_MSG, customerId);
                }
            }
        }
    }

    /**
     * Handle checkout.session.completed event
     */
    private void handleCheckoutSessionCompleted(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing checkout session completed event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) deserializer.getObject().get();
            String subscriptionId = session.getSubscription();
            String customerId = session.getCustomer();

            webhookEvent.setStripeCustomerId(customerId);

            if (customerId == null) {
                log.warn("Checkout session has no customer ID, skipping preceptor lookup");
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);
                    log.info("Found preceptor {} for checkout session", p.getUserId());

                    // Premium status will be set when invoice.paid is received
                    if (subscriptionId != null) {
                        subscriptionService.syncLocalSubscriptionFromStripe(subscriptionId);
                        log.info("Checkout session completed for preceptor: {} with subscription: {}", p.getUserId(), subscriptionId);
                    }
                } else {
                    log.warn(PRECEPTOR_NOT_FOUND_MSG, customerId);
                }
            }
        }
    }

    /**
     * Handle payment_intent.succeeded event - Payment intent successfully processed
     */
    private void handlePaymentIntentSucceeded(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing payment intent succeeded event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().get();

            // Payment intent may not have direct customer reference, log for tracking
            String customerId = paymentIntent.getCustomer();
            if (customerId != null) {
                webhookEvent.setStripeCustomerId(customerId);

                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    webhookEvent.setPreceptor(preceptor.get());
                    log.info("Payment intent succeeded for preceptor: {}", preceptor.get().getUserId());
                }
            }

            log.info("Payment intent succeeded: {} - Amount: {} {}",
                    paymentIntent.getId(),
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency());
        }
    }

    /**
     * Handle payment_intent.payment_failed event - Payment intent failed to process
     */
    private void handlePaymentIntentFailed(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing payment intent failed event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().get();

            // Payment intent may not have direct customer reference
            String customerId = paymentIntent.getCustomer();
            if (customerId != null) {
                webhookEvent.setStripeCustomerId(customerId);

                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    webhookEvent.setPreceptor(preceptor.get());
                    log.warn("Payment intent failed for preceptor: {}", preceptor.get().getUserId());
                }
            }

            log.warn("Payment intent failed: {} - Reason: {} - Amount: {} {}",
                    paymentIntent.getId(),
                    paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage() : "Unknown",
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency());
        }
    }

    /**
     * Extract subscription ID from invoice using Stripe SDK
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
     * Extract payment intent ID from invoice using Stripe SDK
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
     * Handles both String and JSON representations
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

        // Remove surrounding quotes if present (GSON JsonPrimitive)
        if (stringValue.startsWith("\"") && stringValue.endsWith("\"")) {
            stringValue = stringValue.substring(1, stringValue.length() - 1).trim();
        }

        return stringValue.isEmpty() ? null : stringValue;
    }

    /**
     * Extract paid timestamp from invoice status transitions
     */
    private LocalDateTime extractPaidAtTimestamp(Invoice invoice) {
        try {
            Invoice.StatusTransitions transitions = invoice.getStatusTransitions();
            if (transitions != null && transitions.getPaidAt() != null && transitions.getPaidAt() > 0) {
                return LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(transitions.getPaidAt()),
                        ZoneId.systemDefault()
                );
            }
        } catch (Exception e) {
            log.debug("Could not extract paid_at from status transitions: {}", e.getMessage());
        }
        return LocalDateTime.now();
    }

    /**
     * Convert Stripe timestamp to LocalDateTime
     */
    private LocalDateTime convertStripeTimestamp(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp),
                ZoneId.systemDefault()
        );
    }

    /**
     * Save billing transaction from invoice
     * Uses Stripe SDK methods to safely extract invoice data
     */
    private void saveBillingTransaction(Preceptor preceptor, Invoice invoice, TransactionStatus status) {
        try {
            // Extract subscription ID and payment intent ID using Stripe SDK
            String stripeSubscriptionId = extractSubscriptionId(invoice);
            String stripePaymentIntentId = extractPaymentIntentId(invoice);

            // Check if transaction already exists by payment intent ID (primary unique key)
            if (stripePaymentIntentId != null && !stripePaymentIntentId.isEmpty()) {
                Optional<BillingTransaction> existing = billingTransactionRepository
                        .findByStripePaymentIntentId(stripePaymentIntentId);
                if (existing.isPresent()) {
                    log.info("Transaction already exists for payment intent: {}", stripePaymentIntentId);
                    return;
                }
            }

            // Warn if required fields are missing
            if (stripePaymentIntentId == null || stripePaymentIntentId.isEmpty()) {
                log.warn("Invoice {} has no payment intent ID, transaction will have null payment_intent", invoice.getId());
            }
            if (stripeSubscriptionId == null || stripeSubscriptionId.isEmpty()) {
                log.warn("Invoice {} has no subscription ID, transaction will have null subscription", invoice.getId());
            }

            // Build and save transaction
            BillingTransaction transaction = BillingTransaction.builder()
                    .preceptor(preceptor)
                    .stripeInvoiceId(invoice.getId())
                    .stripeSubscriptionId(stripeSubscriptionId)
                    .stripePaymentIntentId(stripePaymentIntentId)
                    .amountInMinorUnits(invoice.getAmountPaid() != null ? invoice.getAmountPaid() : 0L)
                    .currency(invoice.getCurrency())
                    .status(status)
                    .transactionAt(LocalDateTime.now())
                    .build();

            billingTransactionRepository.save(transaction);
            log.info("✓ SAVED BillingTransaction: preceptor={}, invoiceId={}, subscriptionId={}, " +
                            "paymentIntentId={}, amount={}, status={}",
                    preceptor.getUserId(), invoice.getId(), stripeSubscriptionId,
                    stripePaymentIntentId, invoice.getAmountPaid(), status);

        } catch (Exception e) {
            // Log error but don't fail the webhook - constraint violations are expected with duplicate webhook events
            log.warn("Could not save billing transaction for preceptor: {} - {}", preceptor.getUserId(), e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error details:", e);
            }
            // Only throw if it's not a constraint violation
            if (!isConstraintViolation(e)) {
                throw new RuntimeException("Failed to save billing transaction", e);
            }
        }
    }

    /**
     * Check if exception is due to constraint violation
     */
    private boolean isConstraintViolation(Exception e) {
        String message = e.getMessage();
        return message != null && (
                message.contains("duplicate key") ||
                        message.contains("Unique") ||
                        message.contains("constraint")
        );
    }

    /**
     * UPSERT billing invoice using Stripe SDK
     * PostgreSQL ON CONFLICT DO UPDATE ensures idempotency
     * Safe for concurrent webhook events - no SELECT before INSERT
     */
    private void updateOrCreateBillingInvoice(Preceptor preceptor, Invoice invoice, InvoiceStatus status) {
        updateOrCreateBillingInvoice(preceptor, invoice, status, null);
    }

    /**
     * UPSERT billing invoice using Stripe SDK with optional PDF URL
     * PostgreSQL ON CONFLICT DO UPDATE ensures idempotency
     * Safe for concurrent webhook events - no SELECT before INSERT
     *
     * @param preceptor     the preceptor
     * @param invoice       the Stripe invoice
     * @param status        the invoice status
     * @param invoicePdfUrl optional URL/path to generated PDF
     */
    private void updateOrCreateBillingInvoice(Preceptor preceptor, Invoice invoice, InvoiceStatus status, String invoicePdfUrl) {
        try {
            // Extract invoice data using Stripe SDK methods
            String stripeSubscriptionId = extractSubscriptionId(invoice);
            LocalDateTime invoiceCreatedAt = convertStripeTimestamp(invoice.getCreated());
            LocalDateTime invoicePaidAt = (status == InvoiceStatus.PAID) ?
                    extractPaidAtTimestamp(invoice) : null;

            // Safe URLs - null if empty
            String hostedUrl = invoice.getHostedInvoiceUrl();
            String pdfUrl = invoicePdfUrl != null ? invoicePdfUrl : invoice.getInvoicePdf();
            hostedUrl = (hostedUrl != null && !hostedUrl.isEmpty()) ? hostedUrl : null;
            pdfUrl = (pdfUrl != null && !pdfUrl.isEmpty()) ? pdfUrl : null;

            // Safe amounts - default to 0 if null
            Long amountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : 0L;
            Long amountDue = invoice.getAmountDue() != null ? invoice.getAmountDue() : 0L;

            // UPSERT invoice (PostgreSQL: INSERT ... ON CONFLICT DO UPDATE)
            billingInvoiceRepository.upsertInvoice(
                    preceptor.getUserId(),
                    invoice.getId(),
                    invoice.getCustomer(),
                    stripeSubscriptionId,
                    amountPaid,
                    amountDue,
                    invoice.getCurrency(),
                    status.toString(),
                    hostedUrl,
                    pdfUrl,
                    invoiceCreatedAt,
                    invoicePaidAt
            );

            log.info("✓ UPSERT BillingInvoice: preceptor={}, invoiceId={}, subscriptionId={}, " +
                            "status={}, paidAt={}, pdfUrl={}",
                    preceptor.getUserId(), invoice.getId(),
                    stripeSubscriptionId, status, invoicePaidAt, pdfUrl);

        } catch (Exception e) {
            log.error("Error upserting billing invoice for preceptor: {}", preceptor.getUserId(), e);
            throw new RuntimeException("Failed to upsert billing invoice", e);
        }
    }

    /**
     * Handle charge.succeeded event - Payment successfully charged
     */
    private void handleChargeSucceeded(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing charge succeeded event");
        // Informational only - actual payment is tracked via payment_intent.succeeded
        log.debug("Charge succeeded webhook received - payment intent is the primary tracking event");
    }

    /**
     * Handle payment_method.attached event - Payment method attached to customer
     */
    private void handlePaymentMethodAttached(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing payment method attached event");
        // Informational only - payment method attachment doesn't require action
        log.debug("Payment method attached webhook received - customer can now use this payment method");
    }

    /**
     * Handle customer.updated event - Customer record updated
     */
    private void handleCustomerUpdated(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing customer updated event");
        // Informational only - we track customer changes via subscription events
        log.debug("Customer updated webhook received - customer information may have changed");
    }

    /**
     * Handle payment_intent.created event - Payment intent created (initial state)
     */
    private void handlePaymentIntentCreated(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing payment intent created event");
        // Informational only - we track final payment status via payment_intent.succeeded/failed
        log.debug("Payment intent created webhook received - payment is pending");
    }

    /**
     * Handle invoice_payment.paid event - Invoice payment processed
     * NOTE: This event contains InvoicePayment object (different from Invoice)
     * Do NOT route to handleInvoicePaymentSucceeded which expects Invoice
     * Instead, we rely on invoice.payment_succeeded which has the proper Invoice data
     */
    private void handleInvoicePaymentPaid(Event event, WebhookProcessingEvent webhookEvent) {
        log.info("Processing invoice_payment.paid event - skipping to avoid ClassCastException");
        log.debug("This event contains InvoicePayment data type. Using invoice.payment_succeeded (Invoice) for authoritative processing");
        // Do nothing - invoice.payment_succeeded is the authoritative event for invoice payment success
    }

    /**
     * Format amount from minor units to currency format.
     */
    private String formatAmount(Long amountInMinorUnits) {
        double amount = amountInMinorUnits / 100.0;
        return String.format("%.2f", amount);
    }
}