package com.digitalearn.npaxis.subscription.plan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final SubscriptionPlanRepository planRepo;
    private final SubscriptionPlanMapper mapper;

    @Override
    public List<SubscriptionPlanResponse> getActivePlans() {
        return planRepo.findAll()
                .stream()
                .filter(SubscriptionPlan::isActive)
                .map(mapper::toResponse)
                .toList();
    }
}