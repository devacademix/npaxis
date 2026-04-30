package com.digitalearn.npaxis.analytics;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.digitalearn.npaxis.utils.APIConstants.ANALYTICS_API;
import static com.digitalearn.npaxis.utils.APIConstants.ANALYTICS_EVENT_API;
import static com.digitalearn.npaxis.utils.APIConstants.BASE_API;
import static com.digitalearn.npaxis.utils.APIConstants.PRECEPTOR_STATS_API;

/**
 * Controller for analytics.
 */
@RestController
@RequestMapping(BASE_API + "/" + ANALYTICS_API)
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping(ANALYTICS_EVENT_API)
    @Operation(summary = "Log analytics event")
    public ResponseEntity<GenericApiResponse<Void>> logEvent(
            @RequestBody AnalyticsEventRequest request) {

        analyticsService.logEvent(request);

        return ResponseHandler.generateResponse(null, "Event logged successfully", true, HttpStatus.OK);
    }

    @GetMapping(PRECEPTOR_STATS_API)
    @Operation(summary = "Get preceptor stats")
    public ResponseEntity<GenericApiResponse<PreceptorStatsResponse>> getStats(
            @PathVariable Long id) {

        return ResponseHandler.generateResponse(
                analyticsService.getPreceptorStats(id),
                "Stats fetched successfully",
                true, HttpStatus.OK
        );
    }
}