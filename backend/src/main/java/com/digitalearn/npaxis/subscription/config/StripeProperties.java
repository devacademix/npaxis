package com.digitalearn.npaxis.subscription.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "payment.stripe")
public class StripeProperties {

    @NotBlank(message = "Stripe secret key must be configured")
    private String secretKey;

    @NotBlank(message = "Stripe publishable key must be configured")
    private String publishableKey;

    @NotBlank(message = "Stripe webhook secret must be configured")
    private String webhookSecret;

    @NotBlank(message = "Stripe API version must be configured")
    private String apiVersion;

    @Positive
    private int maxNetworkRetries = 3;

    @Positive
    private int connectTimeoutMs = 5_000;

    @Positive
    private int readTimeoutMs = 10_000;

    @NotBlank(message = "Stripe success URL must be configured")
    private String successUrl;
    @NotBlank(message = "Stripe cancel URL must be configured")
    private String cancelUrl;
    private String customerPortalReturnUrl;
    private String currency;
    private Integer defaultTrialDays;
}