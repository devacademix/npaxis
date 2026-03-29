package com.digitalearn.npaxis.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO representing admin dashboard statistics.
 */
@Getter
@Builder
public class AdminStatsResponse {

    // User metrics
    private Long totalUsers;
    private Long totalStudents;
    private Long totalPreceptors;
    private Long newUsersThisMonth;

    // Revenue metrics
    private Long premiumUsersCount;
    private Double totalRevenue;
    private Double monthlyRevenue;

    // Engagement metrics
    private Long totalProfileViews;
    private Long totalContactReveals;
    private Long totalInquiries;

    // Top preceptors
    private List<TopPreceptorDTO> topPreceptors;
}