package com.digitalearn.npaxis.analytics;

import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of analytics service.
 * <p>
 * Responsibilities:
 * - Persist analytics events to database
 * - Retrieve analytics statistics
 * - Handle both legacy (logEvent) and new (trackBackendEvent) tracking approaches
 * <p>
 * ASYNC PROCESSING:
 * - trackBackendEvent uses @Async to avoid blocking request threads
 * - Events are persisted in a separate thread pool
 * - Thread pool configured in application.yml (spring.task.execution)
 * <p>
 * DESIGN NOTES:
 * - logEvent: legacy manual tracking via REST controller
 * - trackBackendEvent: new annotation-driven tracking via AOP
 * - Both methods handle user and preceptor associations independently
 *
 * @author Backend Team
 * @version 1.1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsEventRepository analyticsRepository;
    private final PreceptorRepository preceptorRepository;
    private final UserRepository userRepository;

    /**
     * Legacy method for manual event logging via REST API.
     *
     * @deprecated Use trackBackendEvent() for annotation-driven tracking
     */
    @Override
    public void logEvent(AnalyticsEventRequest request) {

        log.info("Logging event '{}' for preceptor '{}'",
                request.eventType(), request.preceptorId());

        Preceptor preceptor = preceptorRepository.findById(request.preceptorId())
                .orElseThrow(() -> new RuntimeException("Preceptor not found"));

        AnalyticsEvent event = AnalyticsEvent.builder()
                .eventType(request.eventType())
                .preceptor(preceptor)
                .createdAt(LocalDateTime.now())
                .build();

        analyticsRepository.save(event);
    }

    /**
     * Tracks a backend event with full context (AOP/annotation-driven).
     * <p>
     * This method uses @Async to ensure non-blocking performance.
     * Event persistence happens asynchronously in a separate thread.
     * <p>
     * FLOW:
     * 1. Validate and log the tracking request
     * 2. Lookup user entity if userId provided
     * 3. Lookup preceptor if targetId appears to be a preceptor ID
     * 4. Create and persist AnalyticsEvent
     * 5. Log success/failure
     * <p>
     * ERROR HANDLING:
     * - User lookup failures are logged but don't prevent event creation
     * - Preceptor lookup failures are logged but don't prevent event creation
     * - DB persistence failures are logged; events may be queued for retry
     *
     * @param eventType the type of event
     * @param userId    the ID of user triggering event (may be null for anonymous)
     * @param targetId  the ID of primary entity affected (optional)
     * @param metadata  additional context as key-value map
     */
    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trackBackendEvent(
            EventType eventType,
            Long userId,
            String targetId,
            Map<String, Object> metadata) {

        try {
            log.debug("Tracking backend event: eventType={}, userId={}, targetId={}",
                    eventType, userId, targetId);

            // Build the analytics event
            AnalyticsEvent.AnalyticsEventBuilder eventBuilder = AnalyticsEvent.builder()
                    .eventType(eventType)
                    .metadata(metadata)
                    .createdAt(LocalDateTime.now());

            // Lookup and associate user if userId provided
            if (userId != null) {
                try {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        eventBuilder.user(user);
                        log.debug("User associated with event: userId={}", userId);
                    } else {
                        log.warn("User not found for tracking event: userId={}", userId);
                    }
                } catch (Exception e) {
                    log.warn("Error looking up user for analytics event: userId={}", userId, e);
                    // Continue - event still gets tracked even if user lookup fails
                }
            }

            // Lookup and associate preceptor if targetId corresponds to a preceptor
            if (targetId != null && !targetId.isBlank()) {
                try {
                    // Try to parse targetId as Long (for preceptor IDs)
                    Long preceptorId = Long.parseLong(targetId);
                    Preceptor preceptor = preceptorRepository.findById(preceptorId).orElse(null);

                    if (preceptor != null) {
                        eventBuilder.preceptor(preceptor);
                        log.debug("Preceptor associated with event: preceptorId={}", preceptorId);
                    } else {
                        // Preceptor lookup failed - this might be expected if targetId
                        // refers to a different entity type
                        log.debug("Preceptor not found for targetId: {}", targetId);
                    }

                } catch (NumberFormatException e) {
                    // targetId is not a Long - this is fine, may be a string ID or other entity
                    log.debug("TargetId is not a numeric preceptor ID: {}", targetId);
                } catch (Exception e) {
                    log.warn("Error looking up preceptor for analytics event: targetId={}", targetId, e);
                }
            }

            // Persist the event
            AnalyticsEvent event = eventBuilder.build();
            AnalyticsEvent savedEvent = analyticsRepository.save(event);

            log.info("Backend event tracked successfully: eventType={}, eventId={}",
                    eventType, savedEvent.getAnalyticsId());

        } catch (Exception e) {
            // Do NOT propagate exceptions - async method should handle failures gracefully
            log.error("Error tracking backend event: eventType={}, userId={}, targetId={}",
                    eventType, userId, targetId, e);
            // Event may be retried later depending on async executor configuration
        }
    }

    /**
     * Retrieves aggregated statistics for a preceptor.
     * <p>
     * Counts events by type and returns summary statistics.
     *
     * @param preceptorId the ID of the preceptor
     * @return PreceptorStatsResponse with event counts
     */
    @Override
    public PreceptorStatsResponse getPreceptorStats(Long preceptorId) {

        List<Object[]> results =
                analyticsRepository.countEventsByPreceptor(preceptorId);

        long profileViews = 0;
        long contactReveals = 0;
        long inquiries = 0;

        for (Object[] row : results) {
            EventType type = (EventType) row[0];
            long count = (long) row[1];

            switch (type) {
                case PROFILE_VIEWED -> profileViews = count;
                case CONTACT_REVEALED -> contactReveals = count;
                case INQUIRY_SUBMITTED -> inquiries = count;
                // Additional event types can be added here as needed
                default -> log.debug("Unmapped event type in stats: {}", type);
            }
        }

        return PreceptorStatsResponse.builder()
                .profileViews(profileViews)
                .contactReveals(contactReveals)
                .inquiries(inquiries)
                .build();
    }
}

