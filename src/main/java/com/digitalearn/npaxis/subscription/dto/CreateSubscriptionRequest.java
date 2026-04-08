package com.digitalearn.npaxis.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(
        @NotNull Long userId,
        @NotBlank String priceId
) {
}
