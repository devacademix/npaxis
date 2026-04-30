package com.digitalearn.npaxis.admin.dto;

/**
 * DTO for preceptor analytics and engagement metrics
 */
public record PreceptorAnalyticsDTO(
        Long userId,
        String displayName,
        Long totalProfileViews,
        Long totalContactReveals,
        Long totalInquiries,
        Double conversionRate,
        Integer responseTime, // in minutes
        Double averageRating,
        Long activeStudentCount,
        String growthTrend, // UP, DOWN, STABLE
        Double monthlyGrowthPercentage
) {
}

