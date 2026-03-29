package com.digitalearn.npaxis.analytics;

public interface AnalyticsService {

    void logEvent(AnalyticsEventRequest request);

    PreceptorStatsResponse getPreceptorStats(Long preceptorId);
}