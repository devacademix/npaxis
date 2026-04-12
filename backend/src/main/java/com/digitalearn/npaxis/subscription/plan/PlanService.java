package com.digitalearn.npaxis.subscription.plan;

import java.util.List;

public interface PlanService {
    List<SubscriptionPlanResponse> getActivePlans();
}