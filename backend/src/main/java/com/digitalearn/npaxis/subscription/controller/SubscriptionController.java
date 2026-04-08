package com.digitalearn.npaxis.subscription.controller;

import com.digitalearn.npaxis.subscription.dto.CreateSubscriptionRequest;
import com.digitalearn.npaxis.subscription.dto.SubscriptionResponse;
import com.digitalearn.npaxis.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/create")
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscription(request));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelSubscription(@RequestParam Long userId) {
        subscriptionService.cancelSubscription(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionByUserId(userId));
    }
}
