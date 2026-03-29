package com.digitalearn.npaxis.analytics;

import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of analytics service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsEventRepository analyticsRepository;
    private final PreceptorRepository preceptorRepository;

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

    @Override
    public PreceptorStatsResponse getPreceptorStats(Long preceptorId) {

        List<Object[]> results =
                analyticsRepository.countEventsByPreceptor(preceptorId);

        long views = 0, contacts = 0, inquiries = 0;

        for (Object[] row : results) {
            EventType type = (EventType) row[0];
            long count = (long) row[1];

            switch (type) {
                case PROFILE_VIEW -> views = count;
                case CONTACT_REVEAL -> contacts = count;
                case INQUIRY_SENT -> inquiries = count;
            }
        }

        return PreceptorStatsResponse.builder()
                .profileViews(views)
                .contactReveals(contacts)
                .inquiries(inquiries)
                .build();
    }
}