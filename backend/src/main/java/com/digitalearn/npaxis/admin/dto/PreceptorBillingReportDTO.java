package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for preceptor billing and subscription information
 */
public record PreceptorBillingReportDTO(
        Long userId,
        String displayName,
        String subscriptionStatus, // ACTIVE, CANCELED, PAST_DUE
        String subscriptionPlan,
        Double monthlyRevenue,
        Double totalRevenue,
        Integer activeMonths,
        LocalDateTime subscriptionStartDate,
        LocalDateTime subscriptionEndDate,
        String lastPaymentStatus,
        LocalDateTime lastPaymentDate,
        Integer totalPayments,
        Integer failedPayments
) {
}

