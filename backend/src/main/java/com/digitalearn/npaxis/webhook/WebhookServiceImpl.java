package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoice;
import com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoiceRepository;
import com.digitalearn.npaxis.subscription.billing.invoice.InvoiceStatus;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransaction;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransactionRepository;
import com.digitalearn.npaxis.subscription.billing.transaction.TransactionStatus;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for processing Stripe webhook events
 * Handles subscription lifecycle, invoices, and payment events
 * Saves all transactions and updates preceptor premium status
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

    private final WebhookProcessingEventRepository webhookEventRepository;
    private final SubscriptionService subscriptionService;
    private final WebhookEventMapper webhookEventMapper;
    private final PreceptorRepository preceptorRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final BillingTransactionRepository billingTransactionRepository;

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
            webhookEvent.setStatus(WebhookEventStatus.PROCESSED);
            webhookEvent.setProcessedAt(LocalDateTime.now());
            log.info("Event processed successfully: {}", event.getId());

        } catch (Exception e) {
            log.error("Error processing webhook event: {}", event.getId(), e);

            webhookEvent.setStatus(WebhookEventStatus.FAILED);
            webhookEvent.setErrorMessage(e.getMessage());
            webhookEvent.setRetryCount(0);

            // Schedule for retry
            webhookEvent.setNextRetryAt(
                    LocalDateTime.now().plus(INITIAL_RETRY_DELAY_MINUTES, ChronoUnit.MINUTES)
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
                .findByStatusAndNextRetryAtBefore(WebhookEventStatus.FAILED, now);

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
                event.setStatus(WebhookEventStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());
                webhookEventRepository.save(event);

                log.info("Webhook event successfully retried: {}", event.getEventId());

            } catch (Exception e) {
                log.error("Error retrying webhook event: {}", event.getEventId(), e);

                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());

                // Schedule next retry with exponential backoff
                int delayMinutes = INITIAL_RETRY_DELAY_MINUTES * (event.getRetryCount() + 1);
                event.setNextRetryAt(LocalDateTime.now().plus(delayMinutes, ChronoUnit.MINUTES));

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
    private void processEventByType(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
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

            default:
                log.warn("Unhandled webhook event type: {}", eventType);
        }
    }

     /**
      * Handle subscription.created event
      */
     private void handleSubscriptionCreated(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
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
                 log.warn("Subscription {} has no customer ID, skipping preceptor lookup", stripeSubscriptionId);
             } else {
                 // Find and update preceptor
                 Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                 if (preceptor.isPresent()) {
                     Preceptor p = preceptor.get();
                     webhookEvent.setPreceptor(p);
                     log.info("Found preceptor {} for subscription created event", p.getUserId());
                 } else {
                     log.warn("Preceptor not found for customer ID: {}", customerId);
                 }
             }

             // Sync local subscription from Stripe to populate PreceptorSubscription table
             try {
                 subscriptionService.syncLocalSubscriptionFromStripe(stripeSubscriptionId);
                 log.info("Subscription created and synced: {}", stripeSubscriptionId);
             } catch (Exception e) {
                 log.error("Error syncing subscription from stripe: {}", stripeSubscriptionId, e);
                 throw e;
             }
         }
     }

     /**
      * Handle subscription.updated event
      */
     private void handleSubscriptionUpdated(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
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
                 log.warn("Subscription {} has no customer ID, skipping preceptor lookup", stripeSubscriptionId);
             } else {
                 // Find preceptor by customer ID
                 Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                 if (preceptor.isPresent()) {
                     Preceptor p = preceptor.get();
                     webhookEvent.setPreceptor(p);
                     log.info("Found preceptor {} for subscription updated event", p.getUserId());
                     // Premium status will be set when invoice.paid is received
                 } else {
                     log.warn("Preceptor not found for customer ID: {}", customerId);
                 }
             }

             try {
                 subscriptionService.syncLocalSubscriptionFromStripe(stripeSubscriptionId);
                 log.info("Subscription updated and synced: {}", stripeSubscriptionId);
             } catch (Exception e) {
                 log.error("Error syncing subscription from stripe: {}", stripeSubscriptionId, e);
                 throw e;
             }
         }
     }

     /**
      * Handle subscription.deleted event
      */
     private void handleSubscriptionDeleted(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
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
                 log.warn("Subscription {} has no customer ID, skipping preceptor lookup", stripeSubscriptionId);
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
                     log.warn("Preceptor not found for customer ID: {}", customerId);
                 }
             }

             try {
                 subscriptionService.syncLocalSubscriptionFromStripe(stripeSubscriptionId);
                 log.info("Subscription deleted and synced: {}", stripeSubscriptionId);
             } catch (Exception e) {
                 log.error("Error syncing subscription from stripe: {}", stripeSubscriptionId, e);
                 throw e;
             }
         }
     }

    /**
     * Handle invoice.payment_succeeded event - Save transaction
     */
    private void handleInvoicePaymentSucceeded(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
        log.info("Processing invoice payment succeeded event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String customerId = invoice.getCustomer();
            webhookEvent.setStripeCustomerId(customerId);

            if (customerId == null) {
                log.warn("Invoice has no customer ID, cannot process");
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);

                    // Save transaction
                    saveBillingTransaction(p, invoice, TransactionStatus.SUCCEEDED);

                    // Update invoice record
                    updateOrCreateBillingInvoice(p, invoice, InvoiceStatus.PAID);

                    log.info("Invoice payment succeeded for preceptor: {}", p.getUserId());
                } else {
                    log.warn("Preceptor not found for customer ID: {}", customerId);
                }
            }
        }
    }

    /**
     * Handle invoice.payment_failed event - Save failed transaction
     */
    private void handleInvoicePaymentFailed(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
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

                // Update invoice record as open (still needs payment)
                updateOrCreateBillingInvoice(p, invoice, InvoiceStatus.OPEN);

                log.warn("Invoice payment failed for preceptor: {}", p.getUserId());
            }
        }
    }

    /**
     * Handle invoice.created event - Save invoice
     */
    private void handleInvoiceCreated(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
        log.info("Processing invoice created event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String customerId = invoice.getCustomer();
            webhookEvent.setStripeCustomerId(customerId);

            if (customerId == null) {
                log.warn("Invoice has no customer ID, cannot process");
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);

                    // Create invoice record
                    updateOrCreateBillingInvoice(p, invoice, InvoiceStatus.OPEN);

                    log.info("Invoice created for preceptor: {}", p.getUserId());
                } else {
                    log.warn("Preceptor not found for customer ID: {}", customerId);
                }
            }
        }
    }

     /**
      * Handle invoice.paid event - Set preceptor to premium and save transaction
      * This is the key event that confirms payment has been completed
      */
     private void handleInvoicePaid(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
         log.info("Processing invoice paid event");

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
                     log.info("Set preceptor {} to premium on invoice paid", p.getUserId());

                     // Save transaction
                     saveBillingTransaction(p, invoice, TransactionStatus.SUCCEEDED);

                     // Update invoice record - mark as paid
                     updateOrCreateBillingInvoice(p, invoice, InvoiceStatus.PAID);

                     log.info("Invoice marked as paid for preceptor: {}", p.getUserId());
                 } else {
                     log.warn("Preceptor not found for customer ID: {}. Premium status not set.", customerId);
                 }
             }
         }
     }

    /**
     * Handle invoice.finalized event - Invoice is ready for payment
     */
    private void handleInvoiceFinalized(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
        log.info("Processing invoice finalized event");

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String customerId = invoice.getCustomer();
            webhookEvent.setStripeCustomerId(customerId);

            if (customerId == null) {
                log.warn("Invoice has no customer ID, cannot process");
            } else {
                // Find preceptor by customer ID
                Optional<Preceptor> preceptor = preceptorRepository.findByStripeCustomerId(customerId);
                if (preceptor.isPresent()) {
                    Preceptor p = preceptor.get();
                    webhookEvent.setPreceptor(p);

                    // Update invoice record
                    updateOrCreateBillingInvoice(p, invoice, InvoiceStatus.OPEN);

                    log.info("Invoice finalized for preceptor: {}", p.getUserId());
                } else {
                    log.warn("Preceptor not found for customer ID: {}", customerId);
                }
            }
        }
    }

     /**
      * Handle checkout.session.completed event
      */
     private void handleCheckoutSessionCompleted(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
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
                     log.warn("Preceptor not found for customer ID: {}", customerId);
                 }
             }
         }
     }

    /**
     * Handle payment_intent.succeeded event - Payment intent successfully processed
     */
    private void handlePaymentIntentSucceeded(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
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
    private void handlePaymentIntentFailed(Event event, WebhookProcessingEvent webhookEvent) throws Exception {
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
      * Extract data from Stripe invoice using reflection (handles GSON JsonObject)
      */
     private String extractStringFromInvoice(Invoice invoice, String fieldName) {
         try {
             // Use reflection to avoid GSON type reference in the method signature
             java.lang.reflect.Method getRawMethod = invoice.getClass().getMethod("getRawJsonObject");
             Object rawJson = getRawMethod.invoke(invoice);

             if (rawJson == null) {
                 return null;
             }
             // Use reflection to get the method to avoid GSON dependency issues
             java.lang.reflect.Method getMethod = rawJson.getClass().getMethod("get", String.class);
             Object value = getMethod.invoke(rawJson, fieldName);
             if (value != null) {
                 String strValue = value.toString();
                 if (!strValue.isEmpty() && !strValue.equals("null")) {
                     return strValue;
                 }
             }
         } catch (Exception e) {
             log.debug("Could not extract {} from invoice: {}", fieldName, e.getMessage());
         }
         return null;
     }

     /**
      * Extract a string value from Object (handles both String and JSON representations)
      */
     private String extractStringFromObject(Object value) {
         if (value == null) {
             return null;
         }

         String stringValue = value.toString().trim();

         if (stringValue.isEmpty() || "null".equalsIgnoreCase(stringValue) ||
             stringValue.equals("JsonNull") || stringValue.equals("{}")) {
             return null;
         }

         // Remove surrounding quotes if it's a quoted string (GSON JsonPrimitive)
         if (stringValue.startsWith("\"") && stringValue.endsWith("\"")) {
             stringValue = stringValue.substring(1, stringValue.length() - 1).trim();
         }

         if (stringValue.isEmpty()) {
             return null;
         }

         // Simple unescape for common JSON escapes
         return unescapeJsonString(stringValue);
     }

     /**
      * Unescape JSON string literals
      */
     private String unescapeJsonString(String jsonString) {
         if (jsonString == null || !jsonString.contains("\\")) {
             return jsonString;
         }

         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < jsonString.length(); i++) {
             char c = jsonString.charAt(i);
             if (c == '\\' && i + 1 < jsonString.length()) {
                 char nextChar = jsonString.charAt(i + 1);
                 switch (nextChar) {
                     case 'n':
                         sb.append('\n');
                         i++;
                         break;
                     case 'r':
                         sb.append('\r');
                         i++;
                         break;
                     case 't':
                         sb.append('\t');
                         i++;
                         break;
                     case '"':
                         sb.append('"');
                         i++;
                         break;
                     case '\\':
                         sb.append('\\');
                         i++;
                         break;
                     case '/':
                         sb.append('/');
                         i++;
                         break;
                     case 'b':
                         sb.append('\b');
                         i++;
                         break;
                     case 'f':
                         sb.append('\f');
                         i++;
                         break;
                     default:
                         sb.append(c);
                 }
             } else {
                 sb.append(c);
             }
         }
         return sb.toString();
     }

      /**
       * Save billing transaction from invoice
       */
      private void saveBillingTransaction(Preceptor preceptor, Invoice invoice, TransactionStatus status) {
          try {
              // Check if transaction already exists by invoice ID
              Optional<BillingTransaction> existing = billingTransactionRepository
                      .findByStripeInvoiceId(invoice.getId());
              if (existing.isPresent()) {
                  log.info("Transaction already exists for invoice: {}", invoice.getId());
                  return;
              }

              // Extract subscription ID and payment intent ID from invoice using SDK methods
              String stripeSubscriptionId = null;
              String stripePaymentIntentId = null;

              try {
                  Object rawJson = invoice.getRawJsonObject();
                  if (rawJson != null) {
                      // Extract subscription ID
                      Object subValue = rawJson.getClass().getMethod("get", String.class).invoke(rawJson, "subscription");
                      if (subValue != null) {
                          stripeSubscriptionId = extractStringFromObject(subValue);
                      }

                      // Extract payment intent ID
                      Object piValue = rawJson.getClass().getMethod("get", String.class).invoke(rawJson, "payment_intent");
                      if (piValue != null) {
                          stripePaymentIntentId = extractStringFromObject(piValue);
                      }
                  }
              } catch (Exception e) {
                  log.debug("Error extracting from invoice raw JSON: {}", e.getMessage());
              }

              log.info("✓ Extracted from invoice {}: subscription={}, payment_intent={}",
                      invoice.getId(), stripeSubscriptionId, stripePaymentIntentId);

              // Validate that we have the required fields
              if (stripePaymentIntentId == null || stripePaymentIntentId.isEmpty()) {
                  log.warn("⚠ Invoice {} has NO payment intent ID, transaction will have null payment_intent", invoice.getId());
              }
              if (stripeSubscriptionId == null || stripeSubscriptionId.isEmpty()) {
                  log.warn("⚠ Invoice {} has NO subscription ID, transaction will have null subscription", invoice.getId());
              }

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
              log.info("✓ SAVED BillingTransaction for preceptor: {} - " +
                      "stripeInvoiceId: {}, stripeSubscriptionId: {}, stripePaymentIntentId: {}, amount: {}, status: {}",
                      preceptor.getUserId(), invoice.getId(), stripeSubscriptionId,
                      stripePaymentIntentId, invoice.getAmountPaid(), status);

          } catch (Exception e) {
              log.error("Error saving billing transaction for preceptor: {}", preceptor.getUserId(), e);
              throw new RuntimeException("Failed to save billing transaction", e);
          }
      }

     /**
      * Update or create billing invoice
      */
     private void updateOrCreateBillingInvoice(Preceptor preceptor, Invoice invoice, InvoiceStatus status) {
         try {
             Optional<BillingInvoice> existing = billingInvoiceRepository.findByStripeInvoiceId(invoice.getId());

             BillingInvoice billingInvoice;
             if (existing.isPresent()) {
                 billingInvoice = existing.get();
                 log.info("Updating existing invoice: {}", invoice.getId());
             } else {
                 billingInvoice = BillingInvoice.builder()
                         .preceptor(preceptor)
                         .stripeInvoiceId(invoice.getId())
                         .build();
                 log.info("Creating new invoice record: {}", invoice.getId());
             }

             billingInvoice.setStripeCustomerId(invoice.getCustomer());
             billingInvoice.setAmountPaidInMinorUnits(invoice.getAmountPaid() != null ? invoice.getAmountPaid() : 0L);
             billingInvoice.setAmountDueInMinorUnits(invoice.getAmountDue() != null ? invoice.getAmountDue() : 0L);
             billingInvoice.setCurrency(invoice.getCurrency());
             billingInvoice.setStatus(status);

             // Set hosted invoice URL from direct method
             if (invoice.getHostedInvoiceUrl() != null && !invoice.getHostedInvoiceUrl().isEmpty()) {
                 billingInvoice.setHostedInvoiceUrl(invoice.getHostedInvoiceUrl());
                 log.debug("Set hosted invoice URL: {}", invoice.getHostedInvoiceUrl());
             }

             // Set invoice PDF URL
             if (invoice.getInvoicePdf() != null && !invoice.getInvoicePdf().isEmpty()) {
                 billingInvoice.setInvoicePdfUrl(invoice.getInvoicePdf());
                 log.debug("Set invoice PDF URL: {}", invoice.getInvoicePdf());
             }

             // Set stripe subscription ID using raw JSON extraction
              String stripeSubscriptionId = null;
              try {
                  Object rawJson = invoice.getRawJsonObject();
                  if (rawJson != null) {
                      Object subValue = rawJson.getClass().getMethod("get", String.class).invoke(rawJson, "subscription");
                      if (subValue != null) {
                          stripeSubscriptionId = extractStringFromObject(subValue);
                      }
                  }
              } catch (Exception e) {
                  log.debug("Could not extract subscription ID from invoice: {}", e.getMessage());
              }

              if (stripeSubscriptionId != null && !stripeSubscriptionId.isEmpty()) {
                  billingInvoice.setStripeSubscriptionId(stripeSubscriptionId);
                  log.info("✓ Set stripe subscription ID: {}", stripeSubscriptionId);
              } else {
                  log.warn("⚠ Invoice has no subscription ID");
              }

             // Set invoice created at
             if (invoice.getCreated() != null) {
                 billingInvoice.setInvoiceCreatedAt(
                         LocalDateTime.ofInstant(
                                 java.time.Instant.ofEpochSecond(invoice.getCreated()),
                                 ZoneId.systemDefault()
                         )
                 );
             }

             // Set invoice paid at - use status_transitions.paid_at when status is PAID
             if (status == InvoiceStatus.PAID) {
                 Long paidAt = null;
                 try {
                     // Use reflection to avoid GSON type reference
                     java.lang.reflect.Method getRawMethod = invoice.getClass().getMethod("getRawJsonObject");
                     Object rawJson = getRawMethod.invoke(invoice);
                     if (rawJson != null) {
                         Object paidAtObj = rawJson.getClass().getMethod("get", String.class).invoke(rawJson, "status_transitions");
                         if (paidAtObj != null) {
                             // Try to extract paid_at from transitions object
                             try {
                                 Object paidField = paidAtObj.getClass().getMethod("get", String.class).invoke(paidAtObj, "paid_at");
                                 if (paidField != null && paidField instanceof Number) {
                                     paidAt = ((Number) paidField).longValue();
                                 }
                             } catch (Exception ex) {
                                 log.debug("Could not extract paid_at from transitions: {}", ex.getMessage());
                             }
                         }
                     }
                 } catch (Exception e) {
                     log.debug("Could not extract paid_at timestamp from invoice: {}", e.getMessage());
                 }

                 if (paidAt != null && paidAt > 0) {
                     billingInvoice.setInvoicePaidAt(
                             LocalDateTime.ofInstant(
                                     java.time.Instant.ofEpochSecond(paidAt),
                                     ZoneId.systemDefault()
                             )
                     );
                     log.info("Set invoice paid at: {} for invoice: {}", paidAt, invoice.getId());
                 } else {
                     // If we couldn't extract paid_at, use current time as fallback
                     billingInvoice.setInvoicePaidAt(LocalDateTime.now());
                     log.debug("Could not extract paid_at, using current time as fallback for invoice: {}", invoice.getId());
                 }
             }

             billingInvoiceRepository.save(billingInvoice);
             log.info("✓ SAVED BillingInvoice for preceptor: {} - " +
                     "stripeInvoiceId: {}, stripeSubscriptionId: {}, hostedInvoiceUrl: {}, invoicePaidAt: {}, status: {}",
                     preceptor.getUserId(), invoice.getId(), stripeSubscriptionId,
                     invoice.getHostedInvoiceUrl() != null ? "YES" : "NO", billingInvoice.getInvoicePaidAt(), status);

         } catch (Exception e) {
             log.error("Error saving billing invoice for preceptor: {}", preceptor.getUserId(), e);
             throw new RuntimeException("Failed to save billing invoice", e);
         }
     }
}






