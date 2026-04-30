package com.digitalearn.npaxis.analytics;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "APIs for tracking user actions and generating preceptor statistics.")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Log an analytics event", description = "Tracks an event like PROFILE_VIEW, CONTACT_REVEAL, or INQUIRY.")
    @PostMapping("/event")
    public ResponseEntity<GenericApiResponse<Void>> trackEvent(@Valid @RequestBody AnalyticsEventRequest request) {
        log.info("Received request to track event: {} for preceptor ID: {}", request.getEventType(), request.getPreceptorId());
        analyticsService.trackEvent(request);
        return ResponseHandler.generateResponse(null, "Event tracked successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Fetch statistics for a preceptor", description = "Retrieves aggregated counts of profile views, contact reveals, and inquiries.")
    @GetMapping("/preceptors/{id}/stats")
    public ResponseEntity<GenericApiResponse<PreceptorStatsResponse>> getPreceptorStats(@PathVariable("id") Long preceptorId) {
        log.info("Received request to fetch stats for preceptor ID: {}", preceptorId);
        PreceptorStatsResponse stats = analyticsService.getPreceptorStats(preceptorId);
        return ResponseHandler.generateResponse(stats, "Stats fetched successfully", true, HttpStatus.OK);
    }
}
