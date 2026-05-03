package com.digitalearn.npaxis.analytics;

import java.util.Map;

/**
 * Service for analytics event tracking.
 *
 * This service provides methods for:
 * 1. Logging events via REST API (legacy/manual approach)
 * 2. Tracking backend events via AOP (automatic/annotation-driven approach)
 * 3. Retrieving analytics statistics
 *
 * All event tracking is asynchronous to avoid blocking business logic.
 * Events are persisted in the database and can be queried for reports.
 *
 * ============================================
 * IMPLEMENTATION NOTES
 * ============================================
 *
 * - trackBackendEvent() should handle async processing internally
 * - Events must be persisted even if tracking fails
 * - User association is optional (for anonymous tracking)
 * - Preceptor association is determined from targetId or extracted from context
 *
 * @author Backend Team
 * @version 1.1
 */
public interface AnalyticsService {

    /**
     * Logs an analytics event via request (legacy/manual approach).
     *
     * This method is used when manually calling analytics from controllers
     * or services using the request DTO approach.
     *
     * @param request the event request containing eventType and preceptorId
     * @deprecated Prefer trackBackendEvent() for annotation-driven tracking
     */
    void logEvent(AnalyticsEventRequest request);

    /**
     * Tracks a backend event with full context (AOP/annotation-driven approach).
     *
     * This is the new preferred method called automatically by @TrackEvent annotations
     * via the AnalyticsAspect. It provides:
     *
     * - Automatic user extraction (userId from Spring Security)
     * - Flexible target entity identification
     * - Rich metadata capture with HTTP request context
     * - Asynchronous processing to avoid blocking
     *
     * SECURITY NOTE:
     * - userId may be null for anonymous/public endpoints
     * - targetId should represent the primary entity affected
     * - metadata is persisted in JSON, avoid sensitive data
     *
     * PERFORMANCE NOTE:
     * - This method uses @Async internally
     * - Returns immediately without waiting for DB persistence
     * - Failures in async tracking are logged but don't break caller
     *
     * @param eventType the type of event from EventType enum
     * @param userId the ID of the user triggering the event (may be null)
     * @param targetId the ID of the primary entity affected (optional)
     * @param metadata additional context as key-value map (optional)
     *
     * @see TrackEvent
     * @see EventType
     * @see AnalyticsAspect
     */
    void trackBackendEvent(
            EventType eventType,
            Long userId,
            String targetId,
            Map<String, Object> metadata);

    /**
     * Retrieves analytics statistics for a specific preceptor.
     *
     * Returns aggregated event counts by type (views, contacts, inquiries, etc.)
     *
     * @param preceptorId the ID of the preceptor
     * @return PreceptorStatsResponse containing event statistics
     */
    PreceptorStatsResponse getPreceptorStats(Long preceptorId);
}