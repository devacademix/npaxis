package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoiceRepository;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransactionRepository;
import com.digitalearn.npaxis.subscription.core.PreceptorSubscriptionRepository;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class WebhookService {

    private final StripeWebhookEventRepository eventRepo;
    private final PreceptorSubscriptionRepository subscriptionRepo;
    private final BillingInvoiceRepository invoiceRepo;
    private final BillingTransactionRepository txRepo;

    public void process(Event event, String payload) {

        switch (event.getType()) {

            case "checkout.session.completed" -> handleCheckout(event);

            case "invoice.paid" -> handleInvoicePaid(event);

            case "invoice.payment_failed" -> handlePaymentFailed(event);

            case "customer.subscription.deleted" -> handleSubscriptionCanceled(event);
        }

        // SAVE EVENT (IDEMPOTENCY)
        StripeWebhookEvent e = new StripeWebhookEvent();
        e.setEventId(event.getId());
        e.setEventType(event.getType());
        e.setPayload(payload);
        e.setProcessedAt(LocalDateTime.now());

        eventRepo.save(e);
    }

    private void handleCheckout(Event event) {

        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        // Save/update subscription
        // fetch from stripe for details if needed
    }

    private void handleInvoicePaid(Event event) {

        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElseThrow();

        // Save invoice
        // Enable access
    }

    private void handlePaymentFailed(Event event) {
        // mark subscription past_due
    }

    private void handleSubscriptionCanceled(Event event) {
        // disable access
    }
}