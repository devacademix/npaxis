package com.digitalearn.npaxis.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutSessionRequest(
        @NotNull Long preceptorId,
        @NotNull BillingCycle billingCycle,
        @NotBlank String successUrl,
        @NotBlank String cancelUrl
) {
}