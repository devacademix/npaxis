package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.email.EmailService;
import com.digitalearn.npaxis.email.EmailTemplate;
import com.digitalearn.npaxis.subscription.invoice.service.InvoicePdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String PRECEPTOR_NAME_KEY = "preceptorName";
    private static final String PLAN_NAME_KEY = "planName";
    private static final String CURRENCY_KEY = "currency";
    private final EmailService emailService;
    private final ObjectProvider<SubscriptionEmailService> selfProvider;
    private final InvoicePdfService invoicePdfService;

    /**
     * Convert LocalDateTime to Instant for storage in DTO
     */
    private static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.toInstant(ZoneOffset.UTC) : null;
    }

    /**
     * Format date from Instant to display string
     */
    private String formatInstantToDate(Instant instant) {
        if (instant == null) return null;
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DATE_FORMATTER);
    }

    /**
     * Send subscription created email with invoice
     * CRITICAL: This method is called directly from transactional webhook context
     * It extracts data while entities are still attached, then passes DTO to async method
     * DO NOT mark this as @Async - it must run in the webhook transaction
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionCreatedEmail(PreceptorSubscription subscription) {
        try {
            // Initialize all lazy-loaded data in transaction context
            SubscriptionEmailData data = SubscriptionEmailData.builder()
                    .preceptorId(subscription.getPreceptor().getUserId())
                    .preceptorName(subscription.getPreceptor().getUser().getName())
                    .preceptorEmail(subscription.getPreceptor().getUser().getEmail())
                    .planName(subscription.getPlan().getName())
                    .billingInterval(subscription.getPrice().getBillingInterval().toString())
                    .amountInMinorUnits(subscription.getPrice().getAmountInMinorUnits())
                    .currency(subscription.getPrice().getCurrency())
                    .currentPeriodStart(toInstant(subscription.getCurrentPeriodStart()))
                    .nextBillingDate(toInstant(subscription.getNextBillingDate()))
                    .build();

            // Send async email with detached data (subscription object NOT passed to async)
            selfProvider.getObject().sendSubscriptionCreatedEmailAsync(subscription, data);
        } catch (Exception e) {
            log.error("Error preparing subscription created email", e);
        }
    }

    /**
     * Legacy wrapper method - kept for backward compatibility
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionCreatedEmailWrapper(PreceptorSubscription subscription) {
        sendSubscriptionCreatedEmail(subscription);
    }

    /**
     * Send subscription created email (async)
     * CRITICAL: This method only receives DTO - NO entity objects for lazy-loaded access
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendSubscriptionCreatedEmailAsync(PreceptorSubscription subscription, SubscriptionEmailData data) {
        try {
            // Validate email address before attempting to send
            if (data.getPreceptorEmail() == null || data.getPreceptorEmail().isBlank()) {
                log.error("Cannot send subscription created email - preceptor email is null or blank for user: {}", data.getPreceptorId());
                return;
            }

            log.info("Sending subscription created email for user: {}", data.getPreceptorId());

            // Prepare email model using DTO data
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(PRECEPTOR_NAME_KEY, data.getPreceptorName());
            templateModel.put(PLAN_NAME_KEY, data.getPlanName());
            templateModel.put("billingInterval", formatBillingInterval(data.getBillingInterval()));
            templateModel.put("amount", formatAmount(data.getAmountInMinorUnits()));
            templateModel.put(CURRENCY_KEY, data.getCurrency().toUpperCase());
            templateModel.put("periodStart", formatInstantToDate(data.getCurrentPeriodStart()));
            templateModel.put("nextBillingDate", formatInstantToDate(data.getNextBillingDate()));

            // Send email without attachment
            emailService.sendEmail(
                    data.getPreceptorEmail(),
                    EmailTemplate.SUBSCRIPTION_CREATED,
                    templateModel
            );

            log.info("Subscription created email sent successfully for user: {}", data.getPreceptorId());

        } catch (Exception e) {
            log.error("Error sending subscription created email for user: {}", data.getPreceptorId(), e);
            // Don't throw exception - email failures shouldn't break subscription flow
        }
    }


    /**
     * Send subscription upgraded email with invoice
     * CRITICAL: This method is called directly from transactional webhook context
     * It extracts data while entities are still attached, then passes DTO to async method
     * DO NOT mark this as @Async - it must run in the webhook transaction
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionUpgradedEmail(PreceptorSubscription subscription, PreceptorSubscription previousSubscription) {
        try {
            // Initialize all lazy-loaded data in transaction context
            // CRITICAL: Extract ALL data from entities while they're attached
            // The async method will NOT have access to entities - only to this DTO
            SubscriptionEmailData data = SubscriptionEmailData.builder()
                    .preceptorId(subscription.getPreceptor().getUserId())
                    .preceptorName(subscription.getPreceptor().getUser().getName())
                    .preceptorEmail(subscription.getPreceptor().getUser().getEmail())
                    .planName(subscription.getPlan().getName())
                    .oldPlanName(previousSubscription.getPlan().getName())
                    .billingInterval(subscription.getPrice().getBillingInterval().toString())
                    .amountInMinorUnits(subscription.getPrice().getAmountInMinorUnits())
                    .oldAmountInMinorUnits(previousSubscription.getPrice().getAmountInMinorUnits())
                    .currency(subscription.getPrice().getCurrency())
                    .nextBillingDate(toInstant(subscription.getNextBillingDate()))
                    .build();

            // Send async email with detached data (entities NOT passed to async)
            // Note: previousSubscription is NOT passed to async - only DTO
            selfProvider.getObject().sendSubscriptionUpgradedEmailAsync(data);
        } catch (Exception e) {
            log.error("Error preparing subscription upgraded email", e);
        }
    }

    /**
     * Legacy wrapper method - kept for backward compatibility
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionUpgradedEmailWrapper(PreceptorSubscription subscription, PreceptorSubscription previousSubscription) {
        sendSubscriptionUpgradedEmail(subscription, previousSubscription);
    }

    /**
     * Send subscription upgraded email with invoice (async)
     * CRITICAL: This method only receives DTO - NO entity objects
     * All entity access must happen in sync method before calling this
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendSubscriptionUpgradedEmailAsync(SubscriptionEmailData data) {
        try {
            // Validate email address before attempting to send
            if (data.getPreceptorEmail() == null || data.getPreceptorEmail().isBlank()) {
                log.error("Cannot send subscription upgraded email - preceptor email is null or blank for user: {}", data.getPreceptorId());
                return;
            }

            log.info("Sending subscription upgraded email for user: {}", data.getPreceptorId());

            // Prepare email model using ONLY DTO data (no Hibernate session, no entity access)
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(PRECEPTOR_NAME_KEY, data.getPreceptorName());
            templateModel.put("oldPlanName", data.getOldPlanName());
            templateModel.put("newPlanName", data.getPlanName());
            templateModel.put("billingInterval", formatBillingInterval(data.getBillingInterval()));
            templateModel.put("newAmount", formatAmount(data.getAmountInMinorUnits()));
            templateModel.put("oldAmount", formatAmount(data.getOldAmountInMinorUnits()));
            templateModel.put(CURRENCY_KEY, data.getCurrency().toUpperCase());
            templateModel.put("nextBillingDate", formatInstantToDate(data.getNextBillingDate()));

            // Send email with attachment
            emailService.sendEmailWithAttachment(
                    data.getPreceptorEmail(),
                    EmailTemplate.SUBSCRIPTION_UPGRADED,
                    templateModel,
                    null,  // No PDF for upgrade emails
                    null
            );

            log.info("Subscription upgraded email sent successfully for user: {}", data.getPreceptorId());

        } catch (Exception e) {
            log.error("Error sending subscription upgraded email for user: {}", data.getPreceptorId(), e);
            // Don't throw exception - email failures shouldn't break subscription flow
        }
    }


    /**
     * Send subscription canceled email
     * CRITICAL: This method is called directly from transactional webhook context
     * It extracts data while entities are still attached, then passes DTO to async method
     * DO NOT mark this as @Async - it must run in the webhook transaction
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionCanceledEmail(PreceptorSubscription subscription) {
        try {
            // Initialize all lazy-loaded data in transaction context
            SubscriptionEmailData data = SubscriptionEmailData.builder()
                    .preceptorId(subscription.getPreceptor().getUserId())
                    .preceptorName(subscription.getPreceptor().getUser().getName())
                    .preceptorEmail(subscription.getPreceptor().getUser().getEmail())
                    .planName(subscription.getPlan().getName())
                    .canceledAt(toInstant(subscription.getCanceledAt()))
                    .currentPeriodEnd(toInstant(subscription.getCurrentPeriodEnd()))
                    .canceledReason(subscription.getCanceledReason())
                    .build();

            // Send async email with detached data (subscription object NOT passed to async)
            selfProvider.getObject().sendSubscriptionCanceledEmailAsync(data);
        } catch (Exception e) {
            log.error("Error preparing subscription canceled email", e);
        }
    }

    /**
     * Legacy wrapper method - kept for backward compatibility
     */
    @Transactional(readOnly = true)
    public void sendSubscriptionCanceledEmailWrapper(PreceptorSubscription subscription) {
        sendSubscriptionCanceledEmail(subscription);
    }

    /**
     * Send subscription canceled email (async)
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendSubscriptionCanceledEmailAsync(SubscriptionEmailData data) {
        try {
            // Validate email address before attempting to send
            if (data.getPreceptorEmail() == null || data.getPreceptorEmail().isBlank()) {
                log.error("Cannot send subscription canceled email - preceptor email is null or blank for user: {}", data.getPreceptorId());
                return;
            }

            log.info("Sending subscription canceled email for user: {}", data.getPreceptorId());

            // Prepare email model using DTO data
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(PRECEPTOR_NAME_KEY, data.getPreceptorName());
            templateModel.put(PLAN_NAME_KEY, data.getPlanName());
            templateModel.put("canceledAt", formatInstantToDate(data.getCanceledAt()));
            templateModel.put("accessUntil", formatInstantToDate(data.getCurrentPeriodEnd()));
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
     * Send invoice payment email with PDF attachment (sync)
     * CRITICAL: This method is called directly from transactional webhook context
     * It generates and stores the PDF while entities are still attached, then passes to async method
     * DO NOT mark this as @Async - it must run in the webhook transaction
     *
     * @param preceptorId            the preceptor ID
     * @param preceptorName          the preceptor name
     * @param preceptorEmail         the preceptor email
     * @param invoiceNumber          the invoice number
     * @param amountPaidInMinorUnits the amount paid in minor units
     * @param currency               the currency code
     * @param invoiceDate            the invoice creation date
     * @param hostedInvoiceUrl       optional Stripe hosted URL
     * @return the stored PDF URL/path if stored successfully, null otherwise
     */
    @Transactional(readOnly = true)
    public String sendInvoicePaymentEmailWithPdf(
            Long preceptorId,
            String preceptorName,
            String preceptorEmail,
            String invoiceNumber,
            Long amountPaidInMinorUnits,
            String currency,
            LocalDateTime invoiceDate,
            String hostedInvoiceUrl) {
        try {
            log.info("Generating and storing invoice PDF for email for preceptor: {}, invoice: {}",
                    preceptorId, invoiceNumber);

            // Create invoice item list with single subscription charge
            List<InvoicePdfService.InvoiceItemDto> items = new ArrayList<>();
            items.add(new InvoicePdfService.InvoiceItemDto(
                    "Premium Preceptor Subscription",
                    1,
                    amountPaidInMinorUnits,
                    amountPaidInMinorUnits
            ));

            // Generate PDF bytes while in transaction context for email attachment
            byte[] pdfBytes = invoicePdfService.generateInvoicePdfBytes(
                    invoiceNumber,
                    preceptorName,
                    invoiceDate,
                    items,
                    amountPaidInMinorUnits,
                    currency,
                    hostedInvoiceUrl
            );

            log.info("Invoice PDF generated: {} bytes for invoice: {}", pdfBytes.length, invoiceNumber);

            // Call async method to send email with PDF
            selfProvider.getObject().sendInvoicePaymentEmailWithPdfAsync(
                    preceptorId, preceptorName, preceptorEmail,
                    amountPaidInMinorUnits, currency, invoiceNumber,
                    hostedInvoiceUrl, pdfBytes
            );

            // Return null - the async method will handle storage if needed
            return null;

        } catch (Exception e) {
            log.error("Error generating invoice PDF for email: preceptor={}, invoice={}",
                    preceptorId, invoiceNumber, e);
            // Still send email without PDF on PDF generation failure
            selfProvider.getObject().sendInvoicePaymentEmailAsync(
                    preceptorId, preceptorName, preceptorEmail,
                    amountPaidInMinorUnits, currency, invoiceNumber, hostedInvoiceUrl
            );
            return null;
        }
    }

    /**
     * Send invoice payment email with PDF attachment (async)
     * Converts PDF bytes to temporary file and sends via email service
     * <p>
     * IMPORTANT: The temporary file is NOT deleted in this method.
     * It is only marked for deletion on JVM exit via deleteOnExit().
     * This prevents race conditions with the async email service.
     *
     * @param preceptorId            the preceptor ID
     * @param preceptorName          the preceptor name
     * @param preceptorEmail         the preceptor email
     * @param amountPaidInMinorUnits the amount paid
     * @param currency               the currency code
     * @param invoiceNumber          the invoice number
     * @param hostedInvoiceUrl       optional Stripe URL
     * @param pdfBytes               the PDF content as bytes
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendInvoicePaymentEmailWithPdfAsync(Long preceptorId, String preceptorName, String preceptorEmail,
                                                    Long amountPaidInMinorUnits, String currency, String invoiceNumber,
                                                    String hostedInvoiceUrl, byte[] pdfBytes) {
        File tempPdfFile = null;
        try {
            // Validate email address
            if (preceptorEmail == null || preceptorEmail.isBlank()) {
                log.error("Cannot send invoice payment email - preceptor email is null or blank for preceptor: {}, invoice: {}",
                        preceptorId, invoiceNumber);
                return;
            }

            log.info("Sending invoice payment email with PDF attachment for user: {} with invoice: {}", preceptorId, invoiceNumber);

            // Create temporary file from PDF bytes
            tempPdfFile = createTemporaryPdfFile(pdfBytes, invoiceNumber);

            if (tempPdfFile == null || !tempPdfFile.exists()) {
                log.warn("Failed to create temporary PDF file, sending email without attachment");
                sendInvoicePaymentEmailAsync(preceptorId, preceptorName, preceptorEmail,
                        amountPaidInMinorUnits, currency, invoiceNumber, hostedInvoiceUrl);
                return;
            }

            // Prepare email model
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(PRECEPTOR_NAME_KEY, preceptorName);
            templateModel.put("amount", formatAmount(amountPaidInMinorUnits));
            templateModel.put(CURRENCY_KEY, currency.toUpperCase());
            templateModel.put("invoiceNumber", invoiceNumber);
            templateModel.put("hostedInvoiceUrl", hostedInvoiceUrl);

            // Send email with PDF attachment
            String attachmentFileName = "Invoice-" + invoiceNumber + ".pdf";
            emailService.sendEmailWithAttachment(
                    preceptorEmail,
                    EmailTemplate.INVOICE_PAYMENT,
                    templateModel,
                    tempPdfFile,
                    attachmentFileName
            );

            log.info("✓ Invoice payment email with PDF sent successfully for preceptor: {}, invoice: {}",
                    preceptorId, invoiceNumber);

        } catch (Exception e) {
            log.error("Error sending invoice payment email with PDF for preceptor: {}", preceptorId, e);
            // Fallback: send email without attachment
            try {
                sendInvoicePaymentEmailAsync(preceptorId, preceptorName, preceptorEmail,
                        amountPaidInMinorUnits, currency, invoiceNumber, hostedInvoiceUrl);
            } catch (Exception fallbackError) {
                log.error("Fallback email also failed for preceptor: {}", preceptorId, fallbackError);
            }
        }
        // NOTE: Temp file is NOT deleted here. It's marked for deletion on JVM exit via deleteOnExit() in createTemporaryPdfFile()
        // This prevents race conditions where the async email service hasn't finished accessing the file yet.
    }

    /**
     * Create a temporary file from PDF bytes
     *
     * @param pdfBytes      the PDF content as bytes
     * @param invoiceNumber the invoice number (used for naming)
     * @return temporary File object, or null if creation fails
     */
    private File createTemporaryPdfFile(byte[] pdfBytes, String invoiceNumber) {
        try {
            // Create temporary file with meaningful name
            File tempFile = File.createTempFile("invoice-" + invoiceNumber + "-", ".pdf");
            tempFile.deleteOnExit(); // Ensure deletion on JVM exit as fallback

            // Write bytes to file
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
                fos.flush();
            }

            log.debug("Temporary PDF file created: {} ({} bytes)", tempFile.getAbsolutePath(), pdfBytes.length);
            return tempFile;

        } catch (IOException e) {
            log.error("Failed to create temporary PDF file for invoice: {}", invoiceNumber, e);
            return null;
        }
    }

    /**
     * Generate invoice PDF and store it persistently to storage backend.
     * This stores the PDF so it's not lost if email fails to attach it.
     *
     * @param invoiceNumber          the invoice number
     * @param preceptorName          the preceptor name
     * @param invoiceDate            the invoice creation date
     * @param amountPaidInMinorUnits the amount paid
     * @param currency               the currency code
     * @param hostedInvoiceUrl       optional Stripe URL
     * @return the storage URL/path where PDF is stored, or null if storage fails
     */
    public String generateAndStoreInvoicePdf(
            String invoiceNumber,
            String preceptorName,
            LocalDateTime invoiceDate,
            Long amountPaidInMinorUnits,
            String currency,
            String hostedInvoiceUrl) {
        try {
            log.info("Generating and storing invoice PDF for persistent storage: invoice={}", invoiceNumber);

            // Create invoice item list with single subscription charge
            List<InvoicePdfService.InvoiceItemDto> items = new ArrayList<>();
            items.add(new InvoicePdfService.InvoiceItemDto(
                    "Premium Preceptor Subscription",
                    1,
                    amountPaidInMinorUnits,
                    amountPaidInMinorUnits
            ));

            // Generate and store PDF to persistent storage
            String storagePath = invoicePdfService.generateAndStoreInvoicePdf(
                    invoiceNumber,
                    preceptorName,
                    invoiceDate,
                    items,
                    amountPaidInMinorUnits,
                    currency,
                    hostedInvoiceUrl
            );

            log.info("✓ Invoice PDF stored persistently: invoice={}, path={}", invoiceNumber, storagePath);
            return storagePath;

        } catch (Exception e) {
            log.error("Failed to generate and store invoice PDF for persistent storage: invoice={}", invoiceNumber, e);
            return null;
        }
    }

    /**
     * Send invoice payment email (async, without PDF)
     * Called when invoice payment succeeds
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendInvoicePaymentEmailAsync(Long preceptorId, String preceptorName, String preceptorEmail,
                                             Long amountPaidInMinorUnits, String currency, String invoiceNumber,
                                             String hostedInvoiceUrl) {
        try {
            // Validate email address before attempting to send
            if (preceptorEmail == null || preceptorEmail.isBlank()) {
                log.error("Cannot send invoice payment email - preceptor email is null or blank for preceptor: {}, invoice: {}",
                        preceptorId, invoiceNumber);
                return;
            }

            log.info("Sending invoice payment email for user: {} with invoice: {}", preceptorId, invoiceNumber);

            // Prepare email model
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(PRECEPTOR_NAME_KEY, preceptorName);
            templateModel.put("amount", formatAmount(amountPaidInMinorUnits));
            templateModel.put(CURRENCY_KEY, currency.toUpperCase());
            templateModel.put("invoiceNumber", invoiceNumber);
            templateModel.put("hostedInvoiceUrl", hostedInvoiceUrl);

            // Send email without attachment
            emailService.sendEmail(
                    preceptorEmail,
                    EmailTemplate.INVOICE_PAYMENT,
                    templateModel
            );

            log.info("✓ Invoice payment email sent successfully for preceptor: {}, invoice: {}",
                    preceptorId, invoiceNumber);

        } catch (Exception e) {
            log.error("Error sending invoice payment email for preceptor: {}", preceptorId, e);
            // Don't throw exception - email failures shouldn't break subscription flow
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

