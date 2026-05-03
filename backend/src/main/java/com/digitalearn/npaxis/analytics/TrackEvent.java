package com.digitalearn.npaxis.analytics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic analytics event tracking via AOP.
 *
 * This annotation marks methods that should automatically trigger analytics tracking
 * after successful execution. The AnalyticsAspect intercepts annotated methods and
 * extracts:
 * - userId: from Spring Security context
 * - targetId: via Spring Expression Language (SpEL) from method arguments
 * - metadata: optional additional context (also via SpEL)
 *
 * ============================================
 * USAGE EXAMPLES
 * ============================================
 *
 * Example 1: Simple usage without target ID
 * <pre>
 * @TrackEvent(eventType = EventType.USER_LOGIN)
 * public User login(String email, String password) {
 *     ...
 * }
 * </pre>
 *
 * Example 2: With target ID extraction via SpEL
 * <pre>
 * @TrackEvent(
 *     eventType = EventType.PROFILE_VIEWED,
 *     targetIdExpression = "#preceptorId"  // Extracts from method parameter
 * )
 * public PreceptorResponseDTO getPreceptor(Long preceptorId) {
 *     ...
 * }
 * </pre>
 *
 * Example 3: With metadata extraction
 * <pre>
 * @TrackEvent(
 *     eventType = EventType.SEARCH_PERFORMED,
 *     targetIdExpression = "#query",
 *     metadataExpression = "{'filters': #filters.toString(), 'pageSize': #pageable.getPageSize()}"
 * )
 * public Page<PreceptorDTO> search(String query, PreceptorFilter filters, Pageable pageable) {
 *     ...
 * }
 * </pre>
 *
 * ============================================
 * BEST PRACTICES
 * ============================================
 *
 * 1. PREFER SERVICE-LAYER TRACKING
 *    Apply @TrackEvent on service methods for accuracy and security.
 *    Service layer is the "source of truth" for business events.
 *
 * 2. AVOID DUPLICATE TRACKING
 *    Do NOT apply same annotation in both controller and service.
 *    Choose ONE tracking point (prefer service).
 *
 * 3. HANDLE EXCEPTIONS CAREFULLY
 *    By default, events are tracked ONLY on successful execution.
 *    Aspect catches exceptions in its own try-catch to prevent breaking the flow.
 *
 * 4. USE MEANINGFUL TARGET IDs
 *    targetId should represent the primary entity affected.
 *    Examples: preceptorId, userId, enquiryId, subscriptionId
 *
 * 5. KEEP METADATA LIGHTWEIGHT
 *    Metadata is optional. Keep it simple and focused.
 *    Avoid extracting large objects or making additional DB queries.
 *
 * 6. SECURITY CONSIDERATIONS
 *    userId is automatically extracted from SecurityContext.
 *    Do NOT include sensitive data like passwords in metadata.
 *    Metadata is persisted in JSON format - be mindful of PII.
 *
 * ============================================
 * SPEL EXPRESSION NOTES
 * ============================================
 *
 * - Use '#' prefix for parameter names: #preceptorId, #email, #pageable
 * - Access method parameters directly: #preceptorId extracts 'preceptorId' parameter
 * - Use method return types in metadata: #result (if you need the return value)
 * - Complex expressions: `{'key': #param.getValue()}`
 * - NOTE: Avoid expensive operations in SpEL (e.g., DB queries)
 *
 * ============================================
 * PERFORMANCE IMPLICATIONS
 * ============================================
 *
 * - Analytics tracking is ASYNC (non-blocking) by default
 * - AOP aspect is lightweight and won't block request processing
 * - Failures in analytics tracking are caught and logged, not propagated
 * - Thread pool for async tasks is configured in application.yml
 *
 * @author Backend Team
 * @version 1.0
 * @see AnalyticsAspect
 * @see EventType
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackEvent {

    /**
     * The type of event to track.
     * Must be one of the constants defined in EventType enum.
     *
     * @return the EventType to track
     */
    EventType eventType();

    /**
     * Spring Expression Language (SpEL) expression to extract targetId from method arguments.
     *
     * The targetId represents the primary entity affected by the method.
     * Examples:
     * - "#preceptorId" - extracts preceptorId parameter
     * - "#profile.getId()" - calls getId() on profile parameter
     * - "#result.getId()" - extracts ID from return value
     *
     * If empty/blank, targetId will be null in the tracked event.
     *
     * @return SpEL expression to extract targetId, or empty string if not needed
     */
    String targetIdExpression() default "";

    /**
     * Spring Expression Language (SpEL) expression to extract optional metadata.
     *
     * Metadata is stored as a Map and persisted in JSON format.
     * Keep it lightweight and meaningful.
     *
     * Examples:
     * - "{'specialization': #specialty}" - simple key-value
     * - "{'filters': #filters.toString(), 'page': #pageable.getPageNumber()}"
     * - "#result.toMetadata()" - call custom method
     *
     * If empty/blank, metadata will be empty/null.
     *
     * @return SpEL expression to extract metadata, or empty string if no metadata
     */
    String metadataExpression() default "";

}

