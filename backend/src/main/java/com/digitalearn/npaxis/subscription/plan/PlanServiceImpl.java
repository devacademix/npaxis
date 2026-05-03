package com.digitalearn.npaxis.subscription.plan;

import com.digitalearn.npaxis.analytics.AnalyticsService;
import com.digitalearn.npaxis.analytics.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanServiceImpl implements PlanService {

    private final SubscriptionPlanRepository planRepo;
    private final SubscriptionPlanMapper mapper;
    private final AnalyticsService analyticsService;

    @Override
    public List<SubscriptionPlanResponse> getActivePlans() {
        List<SubscriptionPlanResponse> activePlans = planRepo.findAll()
                .stream()
                .filter(SubscriptionPlan::isActive)
                .map(mapper::toResponse)
                .toList();

        // Track page view
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("planCount", activePlans.size());
        analyticsService.trackBackendEvent(
                EventType.SUBSCRIPTION_PAGE_VIEWED,
                null,
                "subscription-plans",
                metadata
        );

        return activePlans;
    }
}