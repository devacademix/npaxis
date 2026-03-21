package com.digitalearn.npaxis.analytics;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO for preceptor analytics stats.
 */
@Getter
@Builder
public class PreceptorStatsResponse {

    private Long profileViews;
    private Long contactReveals;
    private Long inquiries;
}