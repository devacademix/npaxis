package com.digitalearn.npaxis.admin.dto;

import com.digitalearn.npaxis.admin.TopPreceptorDTO;

import java.util.List;

/**
 * DTO for admin analytics overview
 */
public record AdminAnalyticsOverviewDTO(
        Long totalUsers,
        Long totalStudents,
        Long totalPreceptors,
        Long newUsersThisMonth,
        Long premiumUsersCount,
        Double totalRevenue,
        Double monthlyRevenue,
        Long totalProfileViews,
        Long totalContactReveals,
        Long totalInquiries,
        List<TopPreceptorDTO> topPreceptors
) {
}

