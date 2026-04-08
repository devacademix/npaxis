package com.digitalearn.npaxis.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Price ID is required")
        String priceId
) {}
