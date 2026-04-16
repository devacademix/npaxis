package com.digitalearn.npaxis.subscription.plan;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService service;

    @GetMapping
    public ResponseEntity<GenericApiResponse<List<SubscriptionPlanResponse>>> getPlans() {
        return ResponseHandler.generateResponse(service.getActivePlans(), "Successfully fetched all subscription plans", true, HttpStatus.OK);
    }
}