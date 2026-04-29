package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.*;
import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.*;

/**
 * Admin controller for preceptor management operations
 */
@RestController
@RequestMapping(ADMINISTRATION_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Preceptor Management", description = "Admin-only APIs for advanced preceptor management")
public class AdminPreceptorController {

    private final AdminService adminService;

    @Operation(summary = "List all preceptors (admin view)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTORS_LIST_API)
    public ResponseEntity<GenericApiResponse<List<AdminPreceptorListDTO>>> listPreceptors(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin listing all preceptors");
        Page<AdminPreceptorListDTO> preceptors = adminService.listAllPreceptors(pageable);
        return ResponseHandler.generatePaginatedResponse(preceptors, preceptors.getContent(),
                "Preceptors fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Search and filter preceptors (admin view)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTORS_LIST_API + "/search")
    public ResponseEntity<GenericApiResponse<List<AdminPreceptorListDTO>>> searchPreceptors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String verificationStatus,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin searching preceptors");
        Page<AdminPreceptorListDTO> preceptors = adminService.searchPreceptorsAsAdmin(
                specialty, location, verificationStatus, pageable);
        return ResponseHandler.generatePaginatedResponse(preceptors, preceptors.getContent(),
                "Preceptors found successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Get preceptor detail (admin view)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTOR_DETAIL_API)
    public ResponseEntity<GenericApiResponse<AdminPreceptorDetailDTO>> getPreceptorDetail(
            @PathVariable Long userId) {
        log.info("Admin fetching preceptor detail - userId: {}", userId);
        AdminPreceptorDetailDTO preceptor = adminService.getPreceptorDetailAsAdmin(userId);
        return ResponseHandler.generateResponse(preceptor, "Preceptor detail fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Update preceptor (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(ADMIN_PRECEPTOR_UPDATE_API)
    public ResponseEntity<GenericApiResponse<AdminPreceptorDetailDTO>> updatePreceptor(
            @PathVariable Long userId,
            @Valid @RequestBody AdminPreceptorDetailDTO updateDTO) {
        log.info("Admin updating preceptor - userId: {}", userId);
        AdminPreceptorDetailDTO updated = adminService.updatePreceptorAsAdmin(userId, updateDTO);
        return ResponseHandler.generateResponse(updated, "Preceptor updated successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Get approved preceptors")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTORS_APPROVED_API)
    public ResponseEntity<GenericApiResponse<List<AdminPreceptorListDTO>>> getApprovedPreceptors(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin fetching approved preceptors");
        Page<AdminPreceptorListDTO> preceptors = adminService.getApprovedPreceptors(pageable);
        return ResponseHandler.generatePaginatedResponse(preceptors, preceptors.getContent(),
                "Approved preceptors fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Get rejected preceptors")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTORS_REJECTED_API)
    public ResponseEntity<GenericApiResponse<List<AdminPreceptorListDTO>>> getRejectedPreceptors(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin fetching rejected preceptors");
        Page<AdminPreceptorListDTO> preceptors = adminService.getRejectedPreceptors(pageable);
        return ResponseHandler.generatePaginatedResponse(preceptors, preceptors.getContent(),
                "Rejected preceptors fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Get verification history for preceptor")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTOR_VERIFICATION_HISTORY_API)
    public ResponseEntity<GenericApiResponse<List<VerificationHistoryDTO>>> getVerificationHistory(
            @PathVariable Long userId) {
        log.info("Admin fetching verification history - userId: {}", userId);
        List<VerificationHistoryDTO> history = adminService.getPreceptorVerificationHistory(userId);
        return ResponseHandler.generateResponse(history, "Verification history fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Add verification note to preceptor")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(ADMIN_PRECEPTOR_VERIFICATION_NOTES_API)
    public ResponseEntity<GenericApiResponse<String>> addVerificationNote(
            @PathVariable Long userId,
            @RequestParam String note,
            @RequestParam(defaultValue = "REVIEW") String noteType) {
        log.info("Admin adding verification note - userId: {}, type: {}", userId, noteType);
        String result = adminService.addVerificationNote(userId, note, noteType);
        return ResponseHandler.generateResponse(result, "Note added successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Get preceptor billing report")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTOR_BILLING_REPORT_API)
    public ResponseEntity<GenericApiResponse<PreceptorBillingReportDTO>> getBillingReport(
            @PathVariable Long userId) {
        log.info("Admin fetching billing report - userId: {}", userId);
        PreceptorBillingReportDTO report = adminService.getPreceptorBillingReport(userId);
        return ResponseHandler.generateResponse(report, "Billing report fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Get preceptor analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTOR_ANALYTICS_API)
    public ResponseEntity<GenericApiResponse<PreceptorAnalyticsDTO>> getAnalytics(
            @PathVariable Long userId) {
        log.info("Admin fetching analytics - userId: {}", userId);
        PreceptorAnalyticsDTO analytics = adminService.getPreceptorAnalytics(userId);
        return ResponseHandler.generateResponse(analytics, "Analytics fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Download license file (admin audit)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_PRECEPTOR_LICENSE_DOWNLOAD_API)
    public ResponseEntity<org.springframework.core.io.Resource> downloadLicense(
            @PathVariable Long userId) {
        log.info("Admin downloading license - userId: {}", userId);
        org.springframework.core.io.Resource resource = adminService.downloadLicenseAsAdmin(userId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"license.pdf\"")
                .body(resource);
    }
}

