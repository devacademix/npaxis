package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.AdminAnalyticsOverviewDTO;
import com.digitalearn.npaxis.admin.dto.RevenueReportDTO;
import com.digitalearn.npaxis.admin.dto.SystemSettingsDTO;
import com.digitalearn.npaxis.admin.dto.TransactionHistoryDTO;
import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.pdf.service.PdfRequest;
import com.digitalearn.npaxis.pdf.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.digitalearn.npaxis.utils.APIConstants.ADMINISTRATION_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_DASHBOARD_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_DASHBOARD_REPORT_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_REVENUE_SUMMARY_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_REVENUE_TRANSACTION_HISTORY_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_SETTINGS_API;
import static com.digitalearn.npaxis.utils.APIConstants.BASE_API;

/**
 * Admin controller for dashboard, settings, and analytics
 */
@RestController
@RequestMapping(BASE_API + "/" + ADMINISTRATION_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard & Settings", description = "Admin-only APIs for dashboard and system settings")
public class AdminDashboardController {

    private final AdminService adminService;
    private final PdfService pdfService;

    @Operation(summary = "Get admin dashboard overview")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_DASHBOARD_API)
    public ResponseEntity<GenericApiResponse<AdminAnalyticsOverviewDTO>> getDashboard() {
        log.info("Admin fetching dashboard overview");
        AdminAnalyticsOverviewDTO overview = adminService.getAdminDashboardStats();
        return ResponseHandler.generateResponse(overview, "Dashboard stats fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Get all system settings")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_SETTINGS_API)
    public ResponseEntity<GenericApiResponse<List<SystemSettingsDTO>>> getAllSettings() {
        log.info("Admin fetching all settings");
        List<SystemSettingsDTO> settings = adminService.getAllSettings();
        return ResponseHandler.generateResponse(settings, "Settings fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Get specific setting by key")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_SETTINGS_API + "/{key}")
    public ResponseEntity<GenericApiResponse<SystemSettingsDTO>> getSetting(@PathVariable String key) {
        log.info("Admin fetching setting - key: {}", key);
        SystemSettingsDTO setting = adminService.getSetting(key);
        return ResponseHandler.generateResponse(setting, "Setting fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Update setting")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(ADMIN_SETTINGS_API + "/{key}")
    public ResponseEntity<GenericApiResponse<SystemSettingsDTO>> updateSetting(
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        log.info("Admin updating setting - key: {}", key);
        Object value = request.get("value");
        SystemSettingsDTO updated = adminService.updateSetting(key, value);
        return ResponseHandler.generateResponse(updated, "Setting updated successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Get revenue report")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_REVENUE_SUMMARY_API)
    public ResponseEntity<GenericApiResponse<RevenueReportDTO>> getRevenueReport() {
        log.info("Admin fetching revenue report");
        RevenueReportDTO report = adminService.getRevenueReport();
        return ResponseHandler.generateResponse(report, "Revenue report fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Download admin dashboard report as PDF")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = ADMIN_DASHBOARD_REPORT_API, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> downloadDashboardReport() {
        log.info("Admin downloading dashboard report PDF");

        AdminAnalyticsOverviewDTO overview = adminService.getAdminDashboardStats();
        RevenueReportDTO revenueReport = adminService.getRevenueReport();
        LocalDateTime generatedAt = LocalDateTime.now();

        Map<String, Object> data = new HashMap<>();
        data.put("reportSubtitle", "Live admin dashboard export");
        data.put("generatedAt", generatedAt);
        data.put("totalUsers", overview.totalUsers());
        data.put("totalStudents", overview.totalStudents());
        data.put("totalPreceptors", overview.totalPreceptors());
        data.put("premiumUsersCount", overview.premiumUsersCount());
        data.put("newUsersThisMonth", overview.newUsersThisMonth());
        data.put("totalProfileViews", overview.totalProfileViews());
        data.put("totalContactReveals", overview.totalContactReveals());
        data.put("totalInquiries", overview.totalInquiries());
        data.put("topPreceptors", overview.topPreceptors());
        data.put("totalRevenue", revenueReport.totalRevenue());
        data.put("monthlyRevenue", revenueReport.monthlyRecurringRevenue());
        data.put("yearToDateRevenue", revenueReport.yearToDateRevenue());
        data.put("activeSubscriptionsCount", revenueReport.activeSubscriptionsCount());
        data.put("canceledSubscriptionsCount", revenueReport.canceledSubscriptionsCount());
        data.put("churnRate", revenueReport.churnRate());

        PdfRequest request = PdfRequest.builder()
                .templateName("admin-dashboard-report")
                .data(data)
                .outputFileName("admin-dashboard-report-" + generatedAt.toLocalDate())
                .storeFile(false)
                .build();

        byte[] pdfBytes = pdfService.generatePdf(request);
        String fileName = "admin-dashboard-report-" + generatedAt.format(DateTimeFormatter.ISO_DATE) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(pdfBytes.length)
                .body(new ByteArrayResource(pdfBytes));
    }

    @Operation(summary = "Get transaction history")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_REVENUE_TRANSACTION_HISTORY_API)
    public ResponseEntity<GenericApiResponse<List<TransactionHistoryDTO>>> getTransactionHistory(
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("Admin fetching transaction history");
        Page<TransactionHistoryDTO> transactions = adminService.getTransactionHistory(pageable);
        return ResponseHandler.generatePaginatedResponse(transactions, transactions.getContent(),
                "Transaction history fetched successfully", true, HttpStatus.OK);
    }
}

