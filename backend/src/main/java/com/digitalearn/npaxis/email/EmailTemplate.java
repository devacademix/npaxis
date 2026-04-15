package com.digitalearn.npaxis.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplate {

    EMAIL_VERIFICATION("email-verification", "Verify Your NPaxis Account"),
    WELCOME_EMAIL("welcome-email", "Welcome to NPaxis Platform"),
    FORGOT_PASSWORD("forgot-password", "Request to Reset your password."),
    INQUIRY_EMAIL("inquiry-email", "New Inquiry Message"),
    SUBSCRIPTION_CREATED("subscription-created", "Welcome to NPaxis Premium 🎉"),
    SUBSCRIPTION_UPGRADED("subscription-upgraded", "Your Plan Has Been Upgraded ⭐"),
    SUBSCRIPTION_CANCELED("subscription-canceled", "Subscription Cancellation Confirmation"),
    INVOICE_PAYMENT("invoice-payment", "Your NPaxis Invoice Payment Confirmation");

    private final String templateName;
    private final String subject;
}