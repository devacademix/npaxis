package com.digitalearn.npaxis.analytics;

import com.digitalearn.npaxis.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * AOP Aspect for automatic analytics event tracking.
 * <p>
 * This aspect intercepts methods annotated with @TrackEvent and automatically:
 * 1. Executes the method
 * 2. Extracts userId, targetId, and metadata
 * 3. Calls analyticsService to track the event asynchronously
 * 4. Never blocks or breaks the request flow, even if tracking fails
 * <p>
 * ============================================
 * FLOW DIAGRAM
 * ============================================
 * <p>
 * Request comes in
 * ↓
 * Aspect intercepts @TrackEvent method
 * ↓
 * Method executes (joinPoint.proceed())
 * ↓
 * Success! Extract userId, targetId, metadata
 * ↓
 * Create AnalyticsEventRequest
 * ↓
 * Call analyticsService.trackBackendEvent() (ASYNC)
 * ↓
 * Event saved to DB (non-blocking)
 * ↓
 * Return response to client
 * <p>
 * If any tracking fails (exception), it's caught and logged but doesn't
 * break the response flow.
 * <p>
 * ============================================
 * KEY DESIGN PRINCIPLES
 * ============================================
 * <p>
 * 1. NON-INTRUSIVE
 * - Never blocks request processing
 * - Exceptions in tracking don't propagate to business logic
 * - Wrapped in comprehensive try-catch blocks
 * <p>
 * 2. LAZY EVALUATION
 * - userId extracted only if needed
 * - targetId and metadata evaluated via SpEL only when specified
 * - Reduces overhead for simple events
 * <p>
 * 3. SECURITY-AWARE
 * - Validates authentication before using user info
 * - Handles anonymous/unauthenticated users gracefully
 * - Doesn't break public endpoints
 * <p>
 * 4. PERFORMANCE-OPTIMIZED
 * - Aspect is lightweight
 * - SpEL expressions evaluated efficiently
 * - Async tracking keeps main thread free
 * <p>
 * 5. REQUEST-AWARE
 * - Captures HTTP request metadata (IP, user agent)
 * - Useful for detailed analytics
 * - Optional and doesn't break if request context not available
 * <p>
 * ============================================
 * COMMON SCENARIOS
 * ============================================
 * <p>
 * Scenario 1: Unauthenticated user
 * - Event is tracked with userId = null
 * - Useful for tracking public endpoint usage
 * <p>
 * Scenario 2: Exception in tracked method
 * - Method exception propagates normally
 * - Event is NOT tracked (only tracked on success)
 * - Can be changed if needed by modifying the aspect
 * <p>
 * Scenario 3: Invalid SpEL expression
 * - Exception caught, logged with full context
 * - Event still tracked with targetId/metadata = null
 * - Request continues normally
 * <p>
 * Scenario 4: Analytics service down
 * - Exception caught and logged
 * - Main request completes normally
 * - No data loss (async task will retry on next startup... potentially)
 * <p>
 * ============================================
 * TESTING NOTES
 * ============================================
 * <p>
 * - Mock AnalyticsService to verify tracking calls
 * - Verify correct eventType, userId, targetId, metadata extracted
 * - Test exception handling (ensure aspect catches and logs)
 * - Test SpEL expression evaluation
 * - Test unauthenticated access
 *
 * @author Backend Team
 * @version 1.0
 * @see TrackEvent
 * @see EventType
 * @see AnalyticsService
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsAspect {

    private final AnalyticsService analyticsService;

    /**
     * Intercepts all methods annotated with @TrackEvent.
     * <p>
     * IMPORTANT: Uses the @Around advice to ensure:
     * - Method executes first (proceed())
     * - Only on successful execution, tracking is triggered
     * - Exceptions in the method propagate normally
     * - Exceptions in tracking are caught and don't break the flow
     *
     * @param joinPoint  the method invocation join point
     * @param trackEvent the @TrackEvent annotation metadata
     * @return the original method's return value
     * @throws Throwable any exception thrown by the method (not from tracking)
     */
    @Around("@annotation(trackEvent)")
    public Object track(ProceedingJoinPoint joinPoint, TrackEvent trackEvent) throws Throwable {

        // Execute the annotated method first
        // If it throws an exception, it will propagate immediately
        // Tracking only happens on successful execution
        Object result = joinPoint.proceed();

        try {
            // Extract userId from Spring Security context
            Long userId = getCurrentUserId();

            // Extract targetId using SpEL if expression provided
            String targetId = extractTargetId(joinPoint, trackEvent);

            // Extract metadata using SpEL if expression provided
            Map<String, Object> metadata = extractMetadata(joinPoint, trackEvent, result);

            // Build metadata with HTTP request info
            metadata = enrichMetadataWithRequestInfo(metadata);

            // Create request object and call service
            // The service internally handles @Async processing
            trackAnalyticsEvent(
                    trackEvent.eventType(),
                    userId,
                    targetId,
                    metadata
            );

        } catch (Exception e) {
            // CRITICAL: Never let analytics tracking break the business flow
            log.warn("Analytics tracking failed for event: {} (method: {})",
                    trackEvent.eventType(),
                    joinPoint.getSignature().getName(),
                    e);
            // Continue - the request proceeds normally despite tracking failure
        }

        return result;
    }

    /**
     * Extracts the currently authenticated user's ID from Spring Security.
     * <p>
     * Returns Optional.empty() for:
     * - Unauthenticated users (null authentication)
     * - Anonymous users
     * - Non-User principals
     *
     * @return the userId of the currently authenticated user, or empty if not available
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Check if authentication exists and is not anonymous
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return null;
            }

            // Extract principal and check if it's a User
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getUserId();
            }

            log.debug("Principal is not a User instance: {}", principal.getClass().getSimpleName());
            return null;

        } catch (Exception e) {
            log.warn("Error extracting current user ID", e);
            return null;
        }
    }

    /**
     * Extracts targetId from method arguments using Spring Expression Language.
     * <p>
     * If targetIdExpression is empty/blank, returns null.
     * If expression fails to evaluate, logs the error and returns null.
     *
     * @param joinPoint  the method invocation
     * @param trackEvent the annotation metadata containing SpEL expression
     * @return the evaluated targetId or null
     */
    private String extractTargetId(ProceedingJoinPoint joinPoint, TrackEvent trackEvent) {
        String expression = trackEvent.targetIdExpression();

        // If no expression specified, skip extraction
        if (expression == null || expression.isBlank()) {
            return null;
        }

        try {
            Object targetId = evaluateSpELExpression(expression, joinPoint);
            return targetId != null ? targetId.toString() : null;

        } catch (Exception e) {
            log.warn("Error extracting targetId from expression '{}' for method '{}'",
                    expression,
                    joinPoint.getSignature().getName(),
                    e);
            return null;
        }
    }

    /**
     * Extracts metadata from method arguments and return value using SpEL.
     * <p>
     * If metadataExpression is empty/blank, returns empty map.
     * If expression fails or returns non-Map, returns empty map and logs warning.
     *
     * @param joinPoint  the method invocation
     * @param trackEvent the annotation metadata containing SpEL expression
     * @param result     the return value of the method
     * @return evaluated metadata map, or empty map if not available
     */
    private Map<String, Object> extractMetadata(
            ProceedingJoinPoint joinPoint,
            TrackEvent trackEvent,
            Object result) {

        String expression = trackEvent.metadataExpression();

        // If no expression, return empty map
        if (expression == null || expression.isBlank()) {
            return new HashMap<>();
        }

        try {
            Object metadata = evaluateSpELExpression(expression, joinPoint, result);

            // Verify result is a Map
            if (metadata instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadataMap = (Map<String, Object>) metadata;
                return metadataMap;
            } else if (metadata != null) {
                log.warn("Metadata expression did not return a Map. " +
                                "Expression: '{}', Returned type: {}, Returned value: {}",
                        expression,
                        metadata.getClass().getSimpleName(),
                        metadata);
            }

            return new HashMap<>();

        } catch (Exception e) {
            log.warn("Error extracting metadata from expression '{}' for method '{}'",
                    expression,
                    joinPoint.getSignature().getName(),
                    e);
            return new HashMap<>();
        }
    }

    /**
     * Evaluates a Spring Expression Language (SpEL) expression.
     * <p>
     * Available variables in expressions:
     * - #paramName for each method parameter
     * - #result for the return value (if provided)
     * <p>
     * Examples:
     * - "#preceptorId" evaluates the preceptorId parameter
     * - "#result.getId()" calls getId() on the return value
     * - "#pageable.getPageSize()" accesses method on Pageable parameter
     *
     * @param expression the SpEL expression
     * @param joinPoint  the method invocation (provides parameter names and values)
     * @param result     optional return value (for #result variable)
     * @return the evaluated expression result
     */
    private Object evaluateSpELExpression(
            String expression,
            ProceedingJoinPoint joinPoint,
            Object... result) {

        try {
            // Get method parameter names and values
            String[] parameterNames = getParameterNames(joinPoint);
            Object[] parameterValues = joinPoint.getArgs();

            // Create SpEL context with parameters
            // This allows expressions like #preceptorId to resolve correctly
            org.springframework.expression.spel.standard.SpelExpressionParser parser =
                    new org.springframework.expression.spel.standard.SpelExpressionParser();

            org.springframework.expression.spel.support.StandardEvaluationContext context =
                    new org.springframework.expression.spel.support.StandardEvaluationContext();

            // Register all method parameters as variables
            if (parameterNames != null && parameterValues != null) {
                for (int i = 0; i < parameterNames.length && i < parameterValues.length; i++) {
                    context.setVariable(parameterNames[i], parameterValues[i]);
                }
            }

            // Register result variable if provided
            if (result != null && result.length > 0) {
                context.setVariable("result", result[0]);
            }

            // Parse and evaluate the expression
            org.springframework.expression.Expression expr = parser.parseExpression(expression);
            return expr.getValue(context);

        } catch (Exception e) {
            log.warn("Failed to evaluate SpEL expression: '{}'", expression, e);
            throw e;
        }
    }

    /**
     * Extracts method parameter names using Java reflection (via AspectJ).
     * <p>
     * This is essential for SpEL expression evaluation to map parameter names
     * to their actual values.
     *
     * @param joinPoint the method invocation
     * @return array of parameter names, or null if cannot be determined
     */
    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        try {
            org.aspectj.lang.reflect.MethodSignature signature =
                    (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature();
            return signature.getParameterNames();

        } catch (Exception e) {
            log.debug("Could not extract parameter names from method signature", e);
            return null;
        }
    }

    /**
     * Enriches metadata with HTTP request information.
     * <p>
     * This adds context about the HTTP request that triggered the event:
     * - ipAddress: client IP address
     * - userAgent: client browser/user-agent string
     * - endpoint: the requested endpoint path
     * <p>
     * This information is useful for security and traffic analysis.
     * <p>
     * NOTE: Gracefully handles cases where HTTP context is not available
     * (e.g., non-HTTP requests, scheduled tasks)
     *
     * @param metadata the existing metadata map to enrich
     * @return enriched metadata map
     */
    private Map<String, Object> enrichMetadataWithRequestInfo(Map<String, Object> metadata) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Add IP address with fallback for proxy headers
                String ipAddress = getClientIpAddress(request);
                if (ipAddress != null) {
                    metadata.put("ipAddress", ipAddress);
                }

                // Add user-agent string
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    metadata.put("userAgent", userAgent);
                }

                // Add endpoint path
                String endpoint = request.getRequestURI();
                if (endpoint != null) {
                    metadata.put("endpoint", endpoint);
                }

                // Add HTTP method
                String method = request.getMethod();
                if (method != null) {
                    metadata.put("httpMethod", method);
                }
            }

        } catch (Exception e) {
            // Request context may not be available in all scenarios (e.g., async tasks)
            log.debug("Could not extract HTTP request information", e);
        }

        return metadata;
    }

    /**
     * Extracts the client's IP address from the HTTP request.
     * <p>
     * Checks multiple headers to handle proxies:
     * 1. X-Forwarded-For (proxy header)
     * 2. Proxy-Client-IP
     * 3. WL-Proxy-Client-IP
     * 4. HTTP_CLIENT_IP
     * 5. Remote address (direct connection)
     *
     * @param request the HTTP servlet request
     * @return the client IP address, or null if cannot be determined
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check proxy headers first
        String[] proxyHeaders = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP"
        };

        for (String header : proxyHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank()) {
                // X-Forwarded-For may contain multiple IPs, get the first one
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }

        // Fallback to direct remote address
        return request.getRemoteAddr();
    }

    /**
     * Creates and tracks an analytics event through the service.
     * <p>
     * The service internally handles async processing via @Async annotation.
     *
     * @param eventType the type of event
     * @param userId    the ID of the user triggering the event (may be null for anonymous)
     * @param targetId  the ID of the primary entity affected (optional)
     * @param metadata  additional context about the event (optional)
     */
    private void trackAnalyticsEvent(
            EventType eventType,
            Long userId,
            String targetId,
            Map<String, Object> metadata) {

        try {
            log.debug("Tracking event: {} for userId: {}, targetId: {}",
                    eventType, userId, targetId);

            // Call service to track the event
            // Implementation details:
            // 1. Service may internally use @Async to avoid blocking
            // 2. Event saved to AnalyticsEvent entity
            // 3. Event published to event stream if needed (Kafka, etc.)
            analyticsService.trackBackendEvent(eventType, userId, targetId, metadata);

            log.debug("Event tracked successfully: {}", eventType);

        } catch (Exception e) {
            log.error("Error tracking analytics event: {}", eventType, e);
            // Don't re-throw - calling code should continue normally
        }
    }

}

