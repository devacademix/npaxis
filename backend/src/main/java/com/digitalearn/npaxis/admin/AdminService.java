package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.AdminAnalyticsOverviewDTO;
import com.digitalearn.npaxis.admin.dto.AdminPreceptorDetailDTO;
import com.digitalearn.npaxis.admin.dto.AdminPreceptorListDTO;
import com.digitalearn.npaxis.admin.dto.AdminStudentDetailDTO;
import com.digitalearn.npaxis.admin.dto.AdminStudentListDTO;
import com.digitalearn.npaxis.admin.dto.PreceptorAnalyticsDTO;
import com.digitalearn.npaxis.admin.dto.PreceptorBillingReportDTO;
import com.digitalearn.npaxis.admin.dto.RevenueReportDTO;
import com.digitalearn.npaxis.admin.dto.SystemSettingsDTO;
import com.digitalearn.npaxis.admin.dto.TransactionHistoryDTO;
import com.digitalearn.npaxis.admin.dto.VerificationHistoryDTO;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    /**
     * Registers a new admin user.
     *
     * @param request admin registration request
     * @return AdminRegisterResponse with access token
     */
    AdminRegisterResponse registerAdmin(AdminRegisterRequest request);

    /**
     * Fetches all preceptors with PENDING verification status.
     *
     * @return List of pending preceptors.
     */
    Page<Preceptor> getPendingPreceptors(Pageable pageable);

    /**
     * Approves a preceptor request.
     *
     * @param userId The ID of the user (preceptor) to approve.
     * @return Success message.
     */
    String approvePreceptor(Long userId);

    /**
     * Rejects a preceptor request.
     *
     * @param userId The ID of the user (preceptor) to reject.
     * @return Success message.
     */
    String rejectPreceptor(Long userId);

    /**
     * Fetches all users with the ADMIN role.
     *
     * @return List of all admin users.
     */
    List<User> getAllAdmins();

    /**
     * Toggles a user's account state (enabled/disabled).
     *
     * @param userId  The ID of the user.
     * @param enabled The desired state.
     * @return Success message.
     */
    String toggleUserAccount(Long userId, boolean enabled);

    // Admin Dashboard
    AdminStatsResponse getAdminStats();

    /**
     * Get admin dashboard stats with expanded details
     */
    AdminAnalyticsOverviewDTO getAdminDashboardStats();

    // Admin Preceptor Operations

    /**
     * List all preceptors with admin safe data exposure
     */
    Page<AdminPreceptorListDTO> listAllPreceptors(Pageable pageable);

    /**
     * Search and filter preceptors with admin access
     */
    Page<AdminPreceptorListDTO> searchPreceptorsAsAdmin(String specialty, String location, String verificationStatus, Pageable pageable);

    /**
     * Get detailed admin view of a preceptor
     */
    AdminPreceptorDetailDTO getPreceptorDetailAsAdmin(Long userId);

    /**
     * Update preceptor profile as admin
     */
    AdminPreceptorDetailDTO updatePreceptorAsAdmin(Long userId, AdminPreceptorDetailDTO updateDTO);

    /**
     * Get approved preceptors list
     */
    Page<AdminPreceptorListDTO> getApprovedPreceptors(Pageable pageable);

    /**
     * Get rejected preceptors list
     */
    Page<AdminPreceptorListDTO> getRejectedPreceptors(Pageable pageable);

    /**
     * Get verification history for a preceptor
     */
    List<VerificationHistoryDTO> getPreceptorVerificationHistory(Long userId);

    /**
     * Add verification note to preceptor
     */
    String addVerificationNote(Long userId, String note, String noteType);

    /**
     * Get preceptor billing report
     */
    PreceptorBillingReportDTO getPreceptorBillingReport(Long userId);

    /**
     * Get preceptor analytics
     */
    PreceptorAnalyticsDTO getPreceptorAnalytics(Long userId);

    // Admin Student Operations

    /**
     * List all students with admin access
     */
    Page<AdminStudentListDTO> listAllStudents(Pageable pageable);

    /**
     * Search students
     */
    Page<AdminStudentListDTO> searchStudents(String university, String program, Pageable pageable);

    /**
     * Get detailed admin view of a student
     */
    AdminStudentDetailDTO getStudentDetailAsAdmin(Long userId);

    /**
     * Update student profile as admin
     */
    AdminStudentDetailDTO updateStudentAsAdmin(Long userId, AdminStudentDetailDTO updateDTO);

    // Admin Settings Operations

    /**
     * Get all system settings
     */
    List<SystemSettingsDTO> getAllSettings();

    /**
     * Get setting by key
     */
    SystemSettingsDTO getSetting(String key);

    /**
     * Update setting
     */
    SystemSettingsDTO updateSetting(String key, Object value);

    // Admin Revenue Operations

    /**
     * Get revenue summary report
     */
    RevenueReportDTO getRevenueReport();

    /**
     * Get transaction history
     */
    Page<TransactionHistoryDTO> getTransactionHistory(Pageable pageable);

    /**
     * Get revenue by preceptor
     */
    Page<PreceptorBillingReportDTO> getRevenueByPreceptor(Pageable pageable);

    /**
     * Download license file with audit logging
     */
    org.springframework.core.io.Resource downloadLicenseAsAdmin(Long userId);

    /**
     * View/preview license image (returns resource with proper content type for web display)
     * Can display PDF, PNG, JPG, etc. in browser
     */
    org.springframework.core.io.Resource viewLicenseImageAsAdmin(Long userId);

    /**
     * Get preceptor contact information (admin override, no premium check)
     */
    com.digitalearn.npaxis.preceptor.PreceptorContactResponseDTO getPreceptorContactAsAdmin(Long userId);

    /**
     * Reject preceptor with rejection reason
     */
    String rejectPreceptorWithReason(Long userId, String reason);
}
