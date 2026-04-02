package com.digitalearn.npaxis.subscription.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String stripeSessionId,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {
}