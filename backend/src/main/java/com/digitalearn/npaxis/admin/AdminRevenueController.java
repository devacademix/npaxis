package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.PreceptorBillingReportDTO;
import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.ADMINISTRATION_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_REVENUE_BY_PRECEPTOR_API;
import static com.digitalearn.npaxis.utils.APIConstants.BASE_API;

/**
 * Admin controller for revenue and billing reports
 */
@RestController
@RequestMapping(BASE_API + "/" + ADMINISTRATION_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Revenue Management", description = "Admin-only APIs for revenue and billing reports")
public class AdminRevenueController {

    private final AdminService adminService;

    @Operation(summary = "Get revenue by preceptor")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_REVENUE_BY_PRECEPTOR_API)
    public ResponseEntity<GenericApiResponse<List<PreceptorBillingReportDTO>>> getRevenueByPreceptor(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin fetching revenue by preceptor");
        Page<PreceptorBillingReportDTO> revenue = adminService.getRevenueByPreceptor(pageable);
        return ResponseHandler.generatePaginatedResponse(revenue, revenue.getContent(),
                "Revenue by preceptor fetched successfully", true, HttpStatus.OK);
    }
}

