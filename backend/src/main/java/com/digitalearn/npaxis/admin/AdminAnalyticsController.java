package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.AdminAnalyticsOverviewDTO;
import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_ANALYTICS_OVERVIEW_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_ANALYTICS_TOP_PRECEPTORS_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_ANALYTICS_TRENDS_API;
import static com.digitalearn.npaxis.utils.APIConstants.BASE_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMINISTRATION_API;

/**
 * Admin controller for analytics and insights
 */
@RestController
@RequestMapping(BASE_API + "/" + ADMINISTRATION_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Analytics", description = "Admin-only APIs for platform analytics")
public class AdminAnalyticsController {

    private final AdminService adminService;

    @Operation(summary = "Get admin analytics overview")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_ANALYTICS_OVERVIEW_API)
    public ResponseEntity<GenericApiResponse<AdminAnalyticsOverviewDTO>> getAnalyticsOverview() {
        log.info("Admin fetching analytics overview");
        AdminAnalyticsOverviewDTO overview = adminService.getAdminDashboardStats();
        return ResponseHandler.generateResponse(overview, "Analytics overview fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Get top preceptors by analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_ANALYTICS_TOP_PRECEPTORS_API)
    public ResponseEntity<GenericApiResponse<AdminAnalyticsOverviewDTO>> getTopPreceptors() {
        log.info("Admin fetching top preceptors");
        AdminAnalyticsOverviewDTO stats = adminService.getAdminDashboardStats();
        return ResponseHandler.generateResponse(stats, "Top preceptors fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Get platform trends")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_ANALYTICS_TRENDS_API)
    public ResponseEntity<GenericApiResponse<AdminAnalyticsOverviewDTO>> getTrends() {
        log.info("Admin fetching platform trends");
        AdminAnalyticsOverviewDTO stats = adminService.getAdminDashboardStats();
        return ResponseHandler.generateResponse(stats, "Platform trends fetched successfully",
                true, HttpStatus.OK);
    }
}

