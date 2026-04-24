package com.digitalearn.npaxis.subscription.core;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class SubscriptionEmailData {
    private final Long preceptorId;
    private final String preceptorName;
    private final String preceptorEmail;
    private final String planName;
    private final String oldPlanName;
    private final String billingInterval;
    private final Long amountInMinorUnits;
    private final Long oldAmountInMinorUnits;  // For subscription upgrade emails
    private final String currency;
    private final Instant currentPeriodStart;
    private final Instant nextBillingDate;
    private final Instant canceledAt;
    private final Instant currentPeriodEnd;
    private final String canceledReason;
}
