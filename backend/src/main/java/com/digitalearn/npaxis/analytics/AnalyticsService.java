package com.digitalearn.npaxis.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsEventRepository analyticsEventRepository;

    @Transactional
    public void trackEvent(AnalyticsEventRequest request) {
        log.debug("Tracking analytics event: {}", request.getEventType());
        AnalyticsEvent event = AnalyticsEvent.builder()
                .preceptorId(request.getPreceptorId())
                .eventType(request.getEventType())
                .build();
        analyticsEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public PreceptorStatsResponse getPreceptorStats(Long preceptorId) {
        log.debug("Fetching analytics stats for preceptor ID: {}", preceptorId);
        Long profileViews = analyticsEventRepository.countByPreceptorIdAndEventType(preceptorId, "PROFILE_VIEW");
        Long contactReveals = analyticsEventRepository.countByPreceptorIdAndEventType(preceptorId, "CONTACT_REVEAL");
        Long inquiries = analyticsEventRepository.countByPreceptorIdAndEventType(preceptorId, "INQUIRY");

        return PreceptorStatsResponse.builder()
                .profileViews(profileViews)
                .contactReveals(contactReveals)
                .inquiries(inquiries)
                .build();
    }
}
