package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/checkout")
    public CreateCheckoutSessionResponse createCheckoutSession(
            @AuthenticationPrincipal User loggedInUser,
            @RequestBody CreateCheckoutSessionRequest request
    ) {
        return subscriptionService.createCheckoutSession(
                loggedInUser.getUserId(),
                request.priceId()
        );
    }
}