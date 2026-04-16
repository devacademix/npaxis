package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.email.EmailService;
import com.digitalearn.npaxis.email.EmailTemplate;
import com.digitalearn.npaxis.pdf.InvoicePdfService;
import com.digitalearn.npaxis.pdf.SubscriptionInvoiceEmailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;



/**
 * Service for sending subscription-related emails
 * Keeps email logic loosely coupled from subscription business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionEmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private final EmailService emailService;
    private final InvoicePdfService invoicePdfService;

    /**
     * Send subscription created email with invoice
     * This method extracts data in a transactional context to avoid lazy loading issues
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionCreatedEmailWrapper(PreceptorSubscription subscription) {
        try {
            // Initialize all lazy-loaded data in transaction context
            SubscriptionEmailData data = SubscriptionEmailData.builder()
                    .preceptorId(subscription.getPreceptor().getUserId())
                    .preceptorName(subscription.getPreceptor().getName())
                    .preceptorEmail(subscription.getPreceptor().getEmail())
                    .planName(subscription.getPlan().getName())
                    .billingInterval(subscription.getPrice().getBillingInterval().toString())
                    .amountInMinorUnits(subscription.getPrice().getAmountInMinorUnits())
                    .currency(subscription.getPrice().getCurrency())
                    .currentPeriodStart(subscription.getCurrentPeriodStart())
                    .nextBillingDate(subscription.getNextBillingDate())
                    .build();

            // Send async email with detached data
            sendSubscriptionCreatedEmailAsync(subscription, data);
        } catch (Exception e) {
            log.error("Error preparing subscription created email", e);
        }
    }

    /**
     * Send subscription created email with invoice (async)
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendSubscriptionCreatedEmailAsync(PreceptorSubscription subscription, SubscriptionEmailData data) {
        try {
            log.info("Sending subscription created email for user: {}", data.getPreceptorId());

            // Generate invoice PDF using original subscription object
            String invoicePdfPath = invoicePdfService.generateSubscriptionInvoicePdf(subscription, "SUBSCRIPTION_CREATED");
            File invoicePdf = new File(invoicePdfPath);

            // Prepare email model using DTO data
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("preceptorName", data.getPreceptorName());
            templateModel.put("planName", data.getPlanName());
            templateModel.put("billingInterval", formatBillingInterval(data.getBillingInterval()));
            templateModel.put("amount", formatAmount(data.getAmountInMinorUnits()));
            templateModel.put("currency", data.getCurrency().toUpperCase());
            templateModel.put("periodStart", data.getCurrentPeriodStart().format(DATE_FORMATTER));
            templateModel.put("nextBillingDate", data.getNextBillingDate().format(DATE_FORMATTER));

            // Send email with attachment
            emailService.sendEmailWithAttachment(
                    data.getPreceptorEmail(),
                    EmailTemplate.SUBSCRIPTION_CREATED,
                    templateModel,
                    invoicePdf,
                    "NPaxis_Invoice_" + data.getPreceptorId() + ".pdf"
            );

            log.info("Subscription created email sent successfully for user: {}", data.getPreceptorId());

        } catch (Exception e) {
            log.error("Error sending subscription created email for user: {}", data.getPreceptorId(), e);
            // Don't throw exception - email failures shouldn't break subscription flow
        }
    }

    /**
     * Legacy method for backward compatibility - delegates to wrapper
     */
    @Async
    public void sendSubscriptionCreatedEmail(PreceptorSubscription subscription) {
        sendSubscriptionCreatedEmailWrapper(subscription);
    }

    /**
     * Send subscription upgraded email with invoice
     * This method extracts data in a transactional context to avoid lazy loading issues
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionUpgradedEmailWrapper(PreceptorSubscription subscription, PreceptorSubscription previousSubscription) {
        try {
            // Initialize all lazy-loaded data in transaction context
            SubscriptionEmailData data = SubscriptionEmailData.builder()
                    .preceptorId(subscription.getPreceptor().getUserId())
                    .preceptorName(subscription.getPreceptor().getName())
                    .preceptorEmail(subscription.getPreceptor().getEmail())
                    .planName(subscription.getPlan().getName())
                    .oldPlanName(previousSubscription.getPlan().getName())
                    .billingInterval(subscription.getPrice().getBillingInterval().toString())
                    .amountInMinorUnits(subscription.getPrice().getAmountInMinorUnits())
                    .currency(subscription.getPrice().getCurrency())
                    .nextBillingDate(subscription.getNextBillingDate())
                    .build();

            // Send async email with detached data
            sendSubscriptionUpgradedEmailAsync(subscription, previousSubscription, data);
        } catch (Exception e) {
            log.error("Error preparing subscription upgraded email", e);
        }
    }

    /**
     * Send subscription upgraded email with invoice (async)
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendSubscriptionUpgradedEmailAsync(PreceptorSubscription subscription, PreceptorSubscription previousSubscription, SubscriptionEmailData data) {
        try {
            log.info("Sending subscription upgraded email for user: {}", data.getPreceptorId());

            // Generate invoice PDF using original subscription object
            String invoicePdfPath = invoicePdfService.generateSubscriptionInvoicePdf(subscription, "SUBSCRIPTION_UPGRADED");
            File invoicePdf = new File(invoicePdfPath);

            // Prepare email model using DTO data
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("preceptorName", data.getPreceptorName());
            templateModel.put("oldPlanName", data.getOldPlanName());
            templateModel.put("newPlanName", data.getPlanName());
            templateModel.put("billingInterval", formatBillingInterval(data.getBillingInterval()));
            templateModel.put("newAmount", formatAmount(data.getAmountInMinorUnits()));
            templateModel.put("oldAmount", formatAmount(previousSubscription.getPrice().getAmountInMinorUnits()));
            templateModel.put("currency", data.getCurrency().toUpperCase());
            templateModel.put("nextBillingDate", data.getNextBillingDate().format(DATE_FORMATTER));

            // Send email with attachment
            emailService.sendEmailWithAttachment(
                    data.getPreceptorEmail(),
                    EmailTemplate.SUBSCRIPTION_UPGRADED,
                    templateModel,
                    invoicePdf,
                    "NPaxis_Upgrade_Invoice_" + data.getPreceptorId() + ".pdf"
            );

            log.info("Subscription upgraded email sent successfully for user: {}", data.getPreceptorId());

        } catch (Exception e) {
            log.error("Error sending subscription upgraded email for user: {}", data.getPreceptorId(), e);
            // Don't throw exception - email failures shouldn't break subscription flow
        }
    }

    /**
     * Legacy method for backward compatibility - delegates to wrapper
     */
    @Async
    public void sendSubscriptionUpgradedEmail(PreceptorSubscription subscription, PreceptorSubscription previousSubscription) {
        sendSubscriptionUpgradedEmailWrapper(subscription, previousSubscription);
    }

    /**
     * Send subscription canceled email
     * This method extracts data in a transactional context to avoid lazy loading issues
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionCanceledEmailWrapper(PreceptorSubscription subscription) {
        try {
            // Initialize all lazy-loaded data in transaction context
            SubscriptionEmailData data = SubscriptionEmailData.builder()
                    .preceptorId(subscription.getPreceptor().getUserId())
                    .preceptorName(subscription.getPreceptor().getName())
                    .preceptorEmail(subscription.getPreceptor().getEmail())
                    .planName(subscription.getPlan().getName())
                    .canceledAt(subscription.getCanceledAt())
                    .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                    .canceledReason(subscription.getCanceledReason())
                    .build();

            // Send async email with detached data
            sendSubscriptionCanceledEmailAsync(data);
        } catch (Exception e) {
            log.error("Error preparing subscription canceled email", e);
        }
    }

    /**
     * Send subscription canceled email (async)
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendSubscriptionCanceledEmailAsync(SubscriptionEmailData data) {
        try {
            log.info("Sending subscription canceled email for user: {}", data.getPreceptorId());

            // Prepare email model using DTO data
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("preceptorName", data.getPreceptorName());
            templateModel.put("planName", data.getPlanName());
            templateModel.put("canceledAt", data.getCanceledAt().format(DATE_FORMATTER));
            templateModel.put("accessUntil", data.getCurrentPeriodEnd().format(DATE_FORMATTER));
            templateModel.put("canceledReason", data.getCanceledReason() != null ? data.getCanceledReason() : "User requested cancellation");

            // Send email
            emailService.sendEmail(
                    data.getPreceptorEmail(),
                    EmailTemplate.SUBSCRIPTION_CANCELED,
                    templateModel
            );

            log.info("Subscription canceled email sent successfully for user: {}", data.getPreceptorId());

        } catch (Exception e) {
            log.error("Error sending subscription canceled email for user: {}", data.getPreceptorId(), e);
            // Don't throw exception - email failures shouldn't break subscription flow
        }
    }

    /**
     * Legacy method for backward compatibility - delegates to wrapper
     */
    @Async
    public void sendSubscriptionCanceledEmail(PreceptorSubscription subscription) {
        sendSubscriptionCanceledEmailWrapper(subscription);
    }

    /**
     * Send invoice payment succeeded email with Stripe Invoice PDF attachment.
     * <p>
     * This method accepts a DTO containing all necessary data (extracted inside transaction).
     * It runs async without any Hibernate session, preventing lazy-loading errors.
     * <p>
     * DTO-BASED ASYNC DESIGN (CRITICAL FOR ASYNC SAFETY):
     * 1. Webhook handler extracts data INSIDE transaction → creates DTO
     * 2. DTO is detached and passed to this async method
     * 3. This method runs in separate thread with NO Hibernate session
     * 4. Email is sent with PDF attachment from file system
     * 5. No lazy-loading, no Hibernate session leaks
     * <p>
     * IDEMPOTENT & CONCURRENT-SAFE:
     * - If PDF doesn't exist, email is sent without attachment
     * - Each webhook creates independent email task
     * - Safe for concurrent webhook delivery
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendInvoicePaymentEmailAsync(SubscriptionInvoiceEmailDto dto) {
        try {
            log.info("Sending invoice payment email for user: {} with invoice: {}", dto.preceptorId(), dto.invoiceNumber());

            // Prepare email model using DTO data (no DB access - no Hibernate session)
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("preceptorName", dto.preceptorName());
            templateModel.put("planName", dto.planName());
            templateModel.put("amount", formatAmount(dto.amountPaidInMinorUnits()));
            templateModel.put("currency", dto.currency().toUpperCase());
            templateModel.put("invoiceNumber", dto.invoiceNumber());
            templateModel.put("hostedInvoiceUrl", dto.hostedInvoiceUrl());

            // Check if PDF file exists on file system
            File invoicePdf = new File(dto.invoicePdfPath());
            if (invoicePdf.exists()) {
                // SEND WITH ATTACHMENT
                emailService.sendEmailWithAttachment(
                        dto.email(),
                        EmailTemplate.INVOICE_PAYMENT,
                        templateModel,
                        invoicePdf,
                        "NPaxis_Invoice_" + dto.invoiceNumber() + ".pdf"
                );
                log.info("✓ Invoice payment email sent successfully WITH PDF for preceptor: {}, invoice: {}",
                        dto.preceptorId(), dto.invoiceNumber());
            } else {
                // SEND WITHOUT ATTACHMENT if PDF not found (still delivers payment confirmation)
                log.warn("Invoice PDF not found at {}, sending email without attachment for preceptor: {}",
                        dto.invoicePdfPath(), dto.preceptorId());
                emailService.sendEmail(
                        dto.email(),
                        EmailTemplate.INVOICE_PAYMENT,
                        templateModel
                );
                log.info("✓ Invoice payment email sent successfully (no PDF) for preceptor: {}, invoice: {}",
                        dto.preceptorId(), dto.invoiceNumber());
            }

        } catch (Exception e) {
            log.error("Error sending invoice payment email for preceptor: {}", dto.preceptorId(), e);
            // Don't throw exception - email failures shouldn't break subscription flow
            // In production, consider logging to DLQ table for manual retry
        }
    }

    /**
     * Format billing interval for display
     */
    private String formatBillingInterval(String billingInterval) {
        return billingInterval.charAt(0) + billingInterval.substring(1).toLowerCase();
    }

    /**
     * Format amount from minor units to currency format
     */
    private String formatAmount(Long amountInMinorUnits) {
        double amount = amountInMinorUnits / 100.0;
        return String.format("$%.2f", amount);
    }
}

