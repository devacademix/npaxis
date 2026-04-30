package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for revenue summary information
 */
public record RevenueReportDTO(
        Double totalRevenue,
        Double monthlyRecurringRevenue,
        Double yearToDateRevenue,
        Long activeSubscriptionsCount,
        Long canceledSubscriptionsCount,
        Integer averageMonthlyRevenue,
        Double churnRate,
        LocalDateTime reportDate
) {
}

