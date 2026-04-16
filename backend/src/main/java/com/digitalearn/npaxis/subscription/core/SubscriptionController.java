package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "APIs for managing preceptor subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Create checkout session", description = "Initiates subscription via Stripe checkout")
    @ApiResponse(responseCode = "201", description = "Checkout session created successfully")
    @PostMapping("/checkout")
    public ResponseEntity<GenericApiResponse<CreateCheckoutSessionResponse>> createCheckoutSession(
            @AuthenticationPrincipal User loggedInUser,
            @Valid @RequestBody CreateCheckoutSessionRequest request
    ) {
        log.info("Creating checkout session for preceptor: {}", loggedInUser.getUserId());
        CreateCheckoutSessionResponse response = subscriptionService.createCheckoutSession(
                loggedInUser.getUserId(), request.priceId()
        );
        return ResponseHandler.generateResponse(
                response,
                "Checkout session created successfully",
                true,
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "Get subscription status", description = "Fetch current subscription details")
    @ApiResponse(responseCode = "200", description = "Subscription status retrieved")
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<SubscriptionDetailResponse>> getSubscriptionStatus(
            @AuthenticationPrincipal User loggedInUser
    ) {
        log.info("Fetching subscription status for preceptor: {}", loggedInUser.getUserId());
        SubscriptionDetailResponse response = subscriptionService.getSubscriptionDetail(
                loggedInUser.getUserId()
        );
        return ResponseHandler.generateResponse(
                response,
                "Subscription status retrieved successfully",
                true,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Cancel subscription", description = "Cancel subscription at period end (allows usage until expiry)")
    @ApiResponse(responseCode = "200", description = "Subscription will be canceled at period end")
    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Void>> cancelSubscription(
            @AuthenticationPrincipal User loggedInUser
    ) {
        log.info("Canceling subscription for preceptor: {}", loggedInUser.getUserId());
        subscriptionService.cancelSubscription(loggedInUser.getUserId());
        return ResponseHandler.generateResponse(
                null,
                "Subscription will be canceled at period end. You can still access features until then.",
                true,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Update subscription", description = "Change subscription plan or billing interval")
    @ApiResponse(responseCode = "200", description = "Subscription updated successfully")
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Void>> updateSubscription(
            @AuthenticationPrincipal User loggedInUser,
            @Valid @RequestBody UpdateSubscriptionRequest request
    ) {
        log.info("Updating subscription for preceptor: {}", loggedInUser.getUserId());
        subscriptionService.updateSubscription(loggedInUser.getUserId(), request);
        return ResponseHandler.generateResponse(
                null,
                "Subscription updated successfully",
                true,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Get subscription history", description = "List past and current subscriptions with pagination")
    @ApiResponse(responseCode = "200", description = "Subscription history retrieved")
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Page<SubscriptionHistoryResponse>>> getSubscriptionHistory(
            @AuthenticationPrincipal User loggedInUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Fetching subscription history for preceptor: {}", loggedInUser.getUserId());
        Page<SubscriptionHistoryResponse> response = subscriptionService.getSubscriptionHistory(
                loggedInUser.getUserId(), pageable
        );
        return ResponseHandler.<Page<SubscriptionHistoryResponse>>generatePaginatedResponse(
                response,
                response,
                "Subscription history retrieved successfully",
                true,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Create customer portal session", description = "Redirect to Stripe customer portal for billing management")
    @ApiResponse(responseCode = "200", description = "Customer portal session created")
    @GetMapping("/portal")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Map<String, String>>> createCustomerPortal(
            @AuthenticationPrincipal User loggedInUser
    ) {
        log.info("Creating customer portal for preceptor: {}", loggedInUser.getUserId());
        String portalUrl = subscriptionService.createCustomerPortal(loggedInUser.getUserId());
        return ResponseHandler.generateResponse(
                Map.of("portalUrl", portalUrl),
                "Customer portal session created successfully",
                true,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Check premium access", description = "Verify if user can access premium features (includes grace period)")
    @ApiResponse(responseCode = "200", description = "Premium access verified")
    @GetMapping("/access-check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Map<String, Boolean>>> checkPremiumAccess(
            @AuthenticationPrincipal User loggedInUser
    ) {
        boolean hasAccess = subscriptionService.canAccessPremiumFeatures(loggedInUser.getUserId());
        return ResponseHandler.generateResponse(
                Map.of("hasAccess", hasAccess),
                "Premium access verified",
                true,
                HttpStatus.OK
        );
    }
}