package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.*;
import com.digitalearn.npaxis.analytics.AnalyticsEventRepository;
import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.auditing.VerificationAuditLog;
import com.digitalearn.npaxis.auditing.VerificationAuditLogRepository;
import com.digitalearn.npaxis.exceptions.ResourceAlreadyExistsException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.inquiry.InquiryRepository;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.preceptor.PreceptorResponseDTO;
import com.digitalearn.npaxis.preceptor.VerificationStatus;
import com.digitalearn.npaxis.role.Role;
import com.digitalearn.npaxis.role.RoleName;
import com.digitalearn.npaxis.role.RoleRepository;
import com.digitalearn.npaxis.student.Student;
import com.digitalearn.npaxis.student.StudentRepository;
import com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoiceRepository;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransaction;
import com.digitalearn.npaxis.subscription.billing.transaction.BillingTransactionRepository;
import com.digitalearn.npaxis.subscription.billing.transaction.TransactionStatus;
import com.digitalearn.npaxis.subscription.core.PreceptorSubscriptionRepository;
import com.digitalearn.npaxis.subscription.core.SubscriptionStatus;
import com.digitalearn.npaxis.system.SystemSetting;
import com.digitalearn.npaxis.system.SystemSettingRepository;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PreceptorRepository preceptorRepository;
    private final StudentRepository studentRepository;
    private final AnalyticsEventRepository analyticsRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final BillingTransactionRepository billingTransactionRepository;
    private final PreceptorSubscriptionRepository preceptorSubscriptionRepository;
    private final VerificationAuditLogRepository verificationAuditLogRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final InquiryRepository inquiryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdminRegisterResponse registerAdmin(AdminRegisterRequest request) {
        log.info("Registering user with email {} as ADMIN", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("User already exists with Email: " + request.email());
        }

        Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Admin role not found"));

        User user = User.builder()
                .displayName(request.displayName())
                .email(request.email())
                .password(this.passwordEncoder.encode(request.password()))
                .role(adminRole)

                .build();

        userRepository.save(user);

        log.info("Admin '{}' registered successfully", request.email());

        return AdminRegisterResponse.builder()
                .userId(user.getUserId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .role(user.getRole().getRoleName().name())
                .build();
    }

    @Override
    public Page<Preceptor> getPendingPreceptors(Pageable pageable) {
        log.info("Fetching all pending preceptors");
        return preceptorRepository.findByVerificationStatus(pageable, VerificationStatus.PENDING);
    }

    @Override
    @Transactional
    public String approvePreceptor(Long userId) {
        log.info("Approving preceptor with user ID {}", userId);
        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        preceptor.setVerificationStatus(VerificationStatus.APPROVED);
        preceptor.setVerified(true);
        preceptor.setVerificationReviewedAt(LocalDateTime.now());
        preceptorRepository.save(preceptor);

        return "Preceptor request approved successfully";
    }

    @Override
    @Transactional
    public String rejectPreceptor(Long userId) {
        log.info("Rejecting preceptor with user ID {}", userId);
        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        preceptor.setVerificationStatus(VerificationStatus.REJECTED);
        preceptor.setVerified(false);
        preceptor.setVerificationReviewedAt(LocalDateTime.now());
        preceptorRepository.save(preceptor);

        return "Preceptor request rejected successfully";
    }

    @Override
    @Transactional
    public String rejectPreceptorWithReason(Long userId, String reason) {
        log.info("Rejecting preceptor with reason - userId: {}, reason: {}", userId, reason);
        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        VerificationStatus previousStatus = preceptor.getVerificationStatus();
        preceptor.setVerificationStatus(VerificationStatus.REJECTED);
        preceptor.setVerified(false);
        preceptor.setVerificationReviewedAt(LocalDateTime.now());
        preceptor.setCorrectionRequestedReason(reason);
        preceptor.setCorrectionRequestedAt(LocalDateTime.now());
        preceptor.setVerificationAttempts((preceptor.getVerificationAttempts() != null ? preceptor.getVerificationAttempts() : 0) + 1);
        preceptorRepository.save(preceptor);

        // Add audit log entry for rejection with reason
        VerificationAuditLog auditLog = VerificationAuditLog.builder()
                .preceptorId(userId)
                .previousStatus(previousStatus)
                .newStatus(VerificationStatus.REJECTED)
                .reviewerUserId(null) // Can be set from authentication context if needed
                .reviewNote(reason)
                .changeTimestamp(LocalDateTime.now())
                .deleted(false)
                .build();
        verificationAuditLogRepository.save(auditLog);

        return "Preceptor rejected with reason: " + reason;
    }

    @Override
    public List<User> getAllAdmins() {
        log.info("Fetching all admin users");
        Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Admin role not found"));
        return userRepository.findAllByRole(adminRole);
    }

    @Override
    @Transactional
    public String toggleUserAccount(Long userId, boolean enabled) {
        log.info("Toggling account status to {} for user ID {}", enabled, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setAccountEnabled(enabled);
        userRepository.save(user);
        return "User account " + (enabled ? "enabled" : "disabled") + " successfully";
    }

    // Admin Dashboard
    @Override
    public AdminStatsResponse getAdminStats() {

        // --- Users ---
        Long totalUsers = userRepository.count();
        Long totalStudents = studentRepository.count();
        Long totalPreceptors = preceptorRepository.count();

        LocalDateTime startOfMonth =
                YearMonth.now().atDay(1).atStartOfDay();

        Long newUsersThisMonth =
                userRepository.countNewUsersThisMonth(startOfMonth);

        // --- Analytics ---
        List<Object[]> events = analyticsRepository.countAllEvents();

        long views = 0, contacts = 0, inquiries = 0;

        for (Object[] row : events) {
            EventType type = (EventType) row[0];
            long count = (long) row[1];

            switch (type) {
                case PROFILE_VIEW -> views = count;
                case CONTACT_REVEAL -> contacts = count;
                case INQUIRY -> inquiries = count;
            }
        }

//         --- Top Preceptors ---
        List<TopPreceptorDTO> topPreceptors =
                analyticsRepository.findTopPreceptors(EventType.INQUIRY, PageRequest.of(0, 5))
                        .stream()
                        .map(p -> TopPreceptorDTO.builder()
                                .preceptorId(p.getPreceptorId())
                                .name(p.getDisplayName())
                                .inquiries(p.getInquiryCount())
                                .build())
                        .toList();

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalStudents(totalStudents)
                .totalPreceptors(totalPreceptors)
                .newUsersThisMonth(newUsersThisMonth)

                .premiumUsersCount(0L) // placeholder
                .totalRevenue(0.0)
                .monthlyRevenue(0.0)

                .totalProfileViews(views)
                .totalContactReveals(contacts)
                .totalInquiries(inquiries)

                .topPreceptors(topPreceptors)
                .build();
    }

    @Override
    public AdminAnalyticsOverviewDTO getAdminDashboardStats() {
        log.info("Fetching admin dashboard stats");
        AdminStatsResponse stats = this.getAdminStats();
        return new AdminAnalyticsOverviewDTO(
                stats.getTotalUsers(),
                stats.getTotalStudents(),
                stats.getTotalPreceptors(),
                stats.getNewUsersThisMonth(),
                stats.getPremiumUsersCount(),
                stats.getTotalRevenue(),
                stats.getMonthlyRevenue(),
                stats.getTotalProfileViews(),
                stats.getTotalContactReveals(),
                stats.getTotalInquiries(),
                stats.getTopPreceptors()
        );
    }

    @Override
    public Page<AdminPreceptorListDTO> listAllPreceptors(Pageable pageable) {
        log.info("Listing all preceptors for admin");
        Page<Preceptor> preceptors = preceptorRepository.findAllActive(pageable);
        return preceptors.map(this::mapPreceptorToListDTO);
    }

    @Override
    public Page<AdminPreceptorListDTO> searchPreceptorsAsAdmin(String specialty, String location, String verificationStatus, Pageable pageable) {
        log.info("Searching preceptors with filters - specialty: {}, location: {}, status: {}", specialty, location, verificationStatus);
        // Using JpaSpecificationExecutor to build dynamic queries (could be enhanced)
        Page<Preceptor> preceptors = preceptorRepository.findAllActive(pageable);
        return preceptors.map(this::mapPreceptorToListDTO);
    }

    @Override
    public AdminPreceptorDetailDTO getPreceptorDetailAsAdmin(Long userId) {
        log.info("Fetching preceptor detail for admin - userId: {}", userId);
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));
        return mapPreceptorToDetailDTO(preceptor);
    }

    @Override
    @Transactional
    public AdminPreceptorDetailDTO updatePreceptorAsAdmin(Long userId, AdminPreceptorDetailDTO updateDTO) {
        log.info("Admin updating preceptor - userId: {}", userId);
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));
        // Apply updates
        if (updateDTO.displayName() != null) {
            preceptor.getUser().setDisplayName(updateDTO.displayName());
        }
        if (updateDTO.credentials() != null) {
            preceptor.setCredentials(updateDTO.credentials());
        }
        if (updateDTO.specialty() != null) {
            preceptor.setSpecialty(updateDTO.specialty());
        }
        if (updateDTO.location() != null) {
            preceptor.setLocation(updateDTO.location());
        }
        preceptorRepository.save(preceptor);
        log.info("Preceptor updated successfully - userId: {}", userId);
        return mapPreceptorToDetailDTO(preceptor);
    }

    @Override
    public Page<AdminPreceptorListDTO> getApprovedPreceptors(Pageable pageable) {
        log.info("Fetching approved preceptors");
        Page<Preceptor> preceptors = preceptorRepository.findByVerificationStatus(pageable, VerificationStatus.APPROVED);
        return preceptors.map(this::mapPreceptorToListDTO);
    }

    @Override
    public Page<AdminPreceptorListDTO> getRejectedPreceptors(Pageable pageable) {
        log.info("Fetching rejected preceptors");
        Page<Preceptor> preceptors = preceptorRepository.findByVerificationStatus(pageable, VerificationStatus.REJECTED);
        return preceptors.map(this::mapPreceptorToListDTO);
    }

    @Override
    public List<VerificationHistoryDTO> getPreceptorVerificationHistory(Long userId) {
        log.info("Fetching verification history for preceptor - userId: {}", userId);

        // Verify preceptor exists
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        // Fetch all audit logs for this preceptor
        Page<VerificationAuditLog> auditLogs = verificationAuditLogRepository
                .findByPreceptorIdAndDeletedFalseOrderByChangeTimestampDesc(userId, Pageable.ofSize(100));

        // Map to DTO
        return auditLogs.getContent().stream()
                .map(log -> new VerificationHistoryDTO(
                        log.getAuditId(),
                        "VERIFICATION_" + log.getNewStatus().name(),
                        log.getPreviousStatus() != null ? log.getPreviousStatus().name() : null,
                        log.getNewStatus().name(),
                        log.getReviewerUserId() != null ?
                                userRepository.findById(log.getReviewerUserId())
                                        .map(User::getDisplayName).orElse("System") : "System",
                        log.getReviewerUserId(),
                        log.getReviewNote(),
                        log.getChangeTimestamp()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String addVerificationNote(Long userId, String note, String noteType) {
        log.info("Adding verification note for preceptor - userId: {}, noteType: {}", userId, noteType);

        // Verify preceptor exists
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        // Create audit log entry
        VerificationAuditLog auditLog = VerificationAuditLog.builder()
                .preceptorId(userId)
                .previousStatus(preceptor.getVerificationStatus())
                .newStatus(preceptor.getVerificationStatus())
                .reviewNote(note)
                .changeTimestamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        verificationAuditLogRepository.save(auditLog);
        log.info("Verification note added successfully - auditId: {}", auditLog.getAuditId());

        return "Note added successfully";
    }

    @Override
    public PreceptorBillingReportDTO getPreceptorBillingReport(Long userId) {
        log.info("Fetching billing report for preceptor - userId: {}", userId);

        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        // Get all transactions for this preceptor
        Page<BillingTransaction> txPage = billingTransactionRepository
                .findByPreceptor_UserIdOrderByTransactionAtDesc(userId, PageRequest.of(0, Integer.MAX_VALUE));

        List<BillingTransaction> allTransactions = txPage.getContent();

        // Calculate metrics
        double successfulRevenue = allTransactions.stream()
                .filter(tx -> tx.getStatus().toString().equals("SUCCEEDED"))
                .mapToDouble(tx -> tx.getAmountInMinorUnits() / 100.0)
                .sum();

        double failedRevenue = allTransactions.stream()
                .filter(tx -> tx.getStatus().toString().equals("FAILED"))
                .mapToDouble(tx -> tx.getAmountInMinorUnits() / 100.0)
                .sum();

        int successCount = (int) allTransactions.stream()
                .filter(tx -> tx.getStatus().toString().equals("SUCCEEDED"))
                .count();

        int failedCount = (int) allTransactions.stream()
                .filter(tx -> tx.getStatus().toString().equals("FAILED"))
                .count();

        LocalDateTime lastTransactionDate = allTransactions.isEmpty() ?
            null : allTransactions.get(0).getTransactionAt();

        // Get last invoice date
        Page<com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoice> invoicePage =
            billingInvoiceRepository.findByPreceptor_UserIdOrderByInvoiceCreatedAtDesc(
                userId,
                PageRequest.of(0, 1)
            );
        LocalDateTime lastInvoiceDate = invoicePage.isEmpty() ?
            null : invoicePage.getContent().get(0).getInvoiceCreatedAt();

        String status = allTransactions.isEmpty() ? "NO_ACTIVITY" : "ACTIVE";

        log.info("Billing report calculated - userId: {}, revenue: {}, failed: {}, transactions: {}",
                userId, successfulRevenue, failedRevenue, allTransactions.size());

        return new PreceptorBillingReportDTO(
                userId,
                preceptor.getUser().getDisplayName(),
                preceptor.isVerified() ? "VERIFIED" : "PENDING",
                preceptor.isPremium() ? "Premium" : "Standard",
                successfulRevenue,
                failedRevenue,
                successCount,
                lastInvoiceDate,
                lastTransactionDate,
                status,
                null,
                failedCount,
                allTransactions.size()
        );
    }

    @Override
    public PreceptorAnalyticsDTO getPreceptorAnalytics(Long userId) {
        log.info("Fetching analytics for preceptor - userId: {}", userId);

        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        // Get event counts by type for this preceptor
        List<Object[]> eventCounts = analyticsRepository.countEventsByPreceptor(userId);

        long profileViews = 0;
        long contactReveals = 0;
        long inquiries = 0;

        for (Object[] row : eventCounts) {
            EventType type = (EventType) row[0];
            long count = (long) row[1];

            switch (type) {
                case PROFILE_VIEW -> profileViews = count;
                case CONTACT_REVEAL -> contactReveals = count;
                case INQUIRY -> inquiries = count;
            }
        }

        // Calculate response rate (successful inquiries vs total)
        double responseRate = inquiries > 0 ? 100.0 : 0.0;

        // Get transaction history for conversion metrics
        Page<BillingTransaction> transactions = billingTransactionRepository
                .findByPreceptor_UserIdOrderByTransactionAtDesc(userId, PageRequest.of(0, 100));

        long successfulTransactions = transactions.getContent().stream()
                .filter(tx -> tx.getStatus().toString().equals("SUCCEEDED"))
                .count();

        double conversionRate = inquiries > 0 ? (successfulTransactions * 100.0 / inquiries) : 0.0;

        log.info("Analytics calculated - userId: {}, views: {}, contacts: {}, inquiries: {}",
                userId, profileViews, contactReveals, inquiries);

        return new PreceptorAnalyticsDTO(
                userId,
                preceptor.getUser().getDisplayName(),
                profileViews,
                contactReveals,
                inquiries,
                responseRate,
                (int) successfulTransactions,
                conversionRate,
                transactions.getTotalElements(),
                "STABLE",
                0.0  // Placeholder for growth rate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public com.digitalearn.npaxis.preceptor.PreceptorContactResponseDTO getPreceptorContactAsAdmin(Long userId) {
        log.info("Admin fetching preceptor contact info - userId: {}", userId);

        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        // Admin can view contact without premium check
        return com.digitalearn.npaxis.preceptor.PreceptorContactResponseDTO.builder()
                .phone(preceptor.getPhone())
                .email(preceptor.getEmail())
                .build();
    }

    @Override
    public Page<AdminStudentListDTO> listAllStudents(Pageable pageable) {
        log.info("Listing all students for admin");
        Page<Student> students = studentRepository.findAllActive(pageable);
        return students.map(this::mapStudentToListDTO);
    }

    @Override
    public Page<AdminStudentListDTO> searchStudents(String university, String program, Pageable pageable) {
        log.info("Searching students - university: {}, program: {}", university, program);
        Page<Student> students = studentRepository.findAllActive(pageable);
        return students.map(this::mapStudentToListDTO);
    }

    @Override
    public AdminStudentDetailDTO getStudentDetailAsAdmin(Long userId) {
        log.info("Fetching student detail for admin - userId: {}", userId);
        Student student = studentRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + userId));
        return mapStudentToDetailDTO(student);
    }

    @Override
    @Transactional
    public AdminStudentDetailDTO updateStudentAsAdmin(Long userId, AdminStudentDetailDTO updateDTO) {
        log.info("Admin updating student - userId: {}", userId);
        Student student = studentRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + userId));

        // Apply updates
        if (updateDTO.displayName() != null) {
            student.getUser().setDisplayName(updateDTO.displayName());
        }
        if (updateDTO.phone() != null) {
            student.setPhone(updateDTO.phone());
        }
        if (updateDTO.university() != null) {
            student.setUniversity(updateDTO.university());
        }
        if (updateDTO.program() != null) {
            student.setProgram(updateDTO.program());
        }
        if (updateDTO.graduationYear() != null) {
            student.setGraduationYear(updateDTO.graduationYear());
        }

        studentRepository.save(student);
        log.info("Student updated successfully - userId: {}", userId);
        return mapStudentToDetailDTO(student);
    }

    @Override
    public List<SystemSettingsDTO> getAllSettings() {
        log.info("Fetching all system settings");

        List<SystemSetting> settings = systemSettingRepository.findAllActive();

        return settings.stream()
                .map(s -> new SystemSettingsDTO(
                        s.getSettingKey(),
                        s.getSettingValue(),
                        s.getDescription(),
                        s.getCategory().name()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public SystemSettingsDTO getSetting(String key) {
        log.info("Fetching setting - key: {}", key);

        SystemSetting setting = systemSettingRepository.findBySettingKeyAndIsActiveTrue(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        return new SystemSettingsDTO(
                setting.getSettingKey(),
                setting.getSettingValue(),
                setting.getDescription(),
                setting.getCategory().name()
        );
    }

    @Override
    @Transactional
    public SystemSettingsDTO updateSetting(String key, Object value) {
        log.info("Updating setting - key: {}, value: {}", key, value);

        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        setting.setSettingValue(value != null ? value.toString() : "");
        setting.onUpdate();
        systemSettingRepository.save(setting);

        log.info("Setting updated successfully - key: {}", key);

        return new SystemSettingsDTO(
                setting.getSettingKey(),
                setting.getSettingValue(),
                setting.getDescription(),
                setting.getCategory().name()
        );
    }

    @Override
    public RevenueReportDTO getRevenueReport() {
        log.info("Fetching revenue report");

        try {
            LocalDateTime now = LocalDateTime.now();

            // --- Time boundaries (consistent & reusable) ---
            LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

            LocalDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay();
            LocalDateTime startOfNextYear = startOfYear.plusYears(1);

            // --- Revenue queries (fixed repo usage) ---
            long totalMinor = billingInvoiceRepository.getTotalRevenueInMinorUnits();

            long monthMinor = billingInvoiceRepository.getRevenueBetween(
                    startOfMonth, startOfNextMonth
            );

            long ytdMinor = billingInvoiceRepository.getRevenueBetween(
                    startOfYear, startOfNextYear
            );

            // --- Convert safely (use BigDecimal for money) ---
            BigDecimal totalRevenue = BigDecimal.valueOf(totalMinor).movePointLeft(2);
            BigDecimal monthlyRevenue = BigDecimal.valueOf(monthMinor).movePointLeft(2);
            BigDecimal yearToDateRevenue = BigDecimal.valueOf(ytdMinor).movePointLeft(2);

            // --- Counts ---
            long paidInvoicesCount = billingInvoiceRepository.countPaidInvoices();

            // ⚠️ Still approximate — replace later with real subscription table
            long activeSubscriptionsCount =
                    preceptorSubscriptionRepository.countByStatusAndDeletedFalse(SubscriptionStatus.ACTIVE);

            long canceledSubscriptionsCount =
                    preceptorSubscriptionRepository.countByStatusAndDeletedFalse(SubscriptionStatus.CANCELED);

            // --- Average monthly revenue ---
            LocalDateTime firstInvoiceDate = billingInvoiceRepository.getFirstInvoiceDate();

            int avgMonthlyRevenue = 0;
            if (firstInvoiceDate != null) {
                long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(
                        firstInvoiceDate.toLocalDate().withDayOfMonth(1),
                        now.toLocalDate().withDayOfMonth(1)
                );

                if (monthsBetween > 0) {
                    avgMonthlyRevenue = totalRevenue
                            .divide(BigDecimal.valueOf(monthsBetween), RoundingMode.HALF_UP)
                            .intValue();
                }
            }

            // --- Churn ---
            long totalSubscriptions = activeSubscriptionsCount + canceledSubscriptionsCount;

            double churnRate = totalSubscriptions > 0
                    ? (double) canceledSubscriptionsCount / totalSubscriptions * 100
                    : 0.0;

            log.info("✓ Revenue report calculated: total={}, monthly={}, ytd={}",
                    totalRevenue, monthlyRevenue, yearToDateRevenue);

            return new RevenueReportDTO(
                    totalRevenue.doubleValue(),
                    monthlyRevenue.doubleValue(),
                    yearToDateRevenue.doubleValue(),
                    activeSubscriptionsCount,
                    canceledSubscriptionsCount,
                    avgMonthlyRevenue,
                    churnRate,
                    now
            );

        } catch (Exception e) {
            log.error("Error fetching revenue report", e);

            return new RevenueReportDTO(
                    0.0, 0.0, 0.0,
                    0L, 0L, 0,
                    0.0,
                    LocalDateTime.now()
            );
        }
    }

    @Override
    public Page<TransactionHistoryDTO> getTransactionHistory(Pageable pageable) {
        log.info("Fetching transaction history");

        try {
            // Fetch all transactions ordered by date (newest first)
            Page<BillingTransaction> transactions = billingTransactionRepository
                    .findAllWithPreceptorOrderByTransactionAtDesc(pageable);

            // Map to DTO
            return transactions.map(tx -> {
                Double amount = tx.getAmountInMinorUnits() / 100.0;
                String displayName = tx.getPreceptor() != null ?
                    tx.getPreceptor().getUser().getDisplayName() : "Unknown";
                Long userId = tx.getPreceptor() != null ?
                    tx.getPreceptor().getUserId() : null;
                String transactionType = "PAYMENT"; // Default type
                String paymentMethod = "Stripe";

                log.debug("Transaction: invoiceId={}, preceptor={}, amount={}, status={}",
                        tx.getStripeInvoiceId(), userId, amount, tx.getStatus());

                return new TransactionHistoryDTO(
                        tx.getStripePaymentIntentId() != null ?
                            tx.getStripePaymentIntentId() : tx.getId().toString(),
                        userId,
                        displayName,
                        transactionType,
                        amount,
                        tx.getStatus().toString(),
                        paymentMethod,
                        tx.getTransactionAt()
                );
            });

        } catch (Exception e) {
            log.error("Error fetching transaction history: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<PreceptorBillingReportDTO> getRevenueByPreceptor(Pageable pageable) {
        log.info("Fetching revenue by preceptor");

        try {
            // Fetch all preceptors
            Page<Preceptor> preceptors = preceptorRepository.findAllActive(pageable);

            // Map each preceptor to billing report with aggregated data
            return preceptors.map(preceptor -> {
                // Get all transactions for this preceptor
                Page<BillingTransaction> txPage = billingTransactionRepository
                        .findByPreceptor_UserIdOrderByTransactionAtDesc(preceptor.getUserId(),
                            PageRequest.of(0, Integer.MAX_VALUE));

                List<BillingTransaction> allTransactions = txPage.getContent();

                // Calculate metrics
                double successfulRevenue = allTransactions.stream()
                        .filter(tx -> tx.getStatus().toString().equals("SUCCEEDED"))
                        .mapToDouble(tx -> tx.getAmountInMinorUnits() / 100.0)
                        .sum();

                double failedRevenue = allTransactions.stream()
                        .filter(tx -> tx.getStatus().toString().equals("FAILED"))
                        .mapToDouble(tx -> tx.getAmountInMinorUnits() / 100.0)
                        .sum();

                int successCount = (int) allTransactions.stream()
                        .filter(tx -> tx.getStatus().toString().equals("SUCCEEDED"))
                        .count();

                int failedCount = (int) allTransactions.stream()
                        .filter(tx -> tx.getStatus().toString().equals("FAILED"))
                        .count();

                LocalDateTime lastTransactionDate = allTransactions.isEmpty() ?
                    null : allTransactions.get(0).getTransactionAt();

                // Get last invoice date
                Page<com.digitalearn.npaxis.subscription.billing.invoice.BillingInvoice> invoicePage =
                    billingInvoiceRepository.findByPreceptor_UserIdOrderByInvoiceCreatedAtDesc(
                        preceptor.getUserId(),
                        PageRequest.of(0, 1)
                    );
                LocalDateTime lastInvoiceDate = invoicePage.isEmpty() ?
                    null : invoicePage.getContent().get(0).getInvoiceCreatedAt();

                String status = allTransactions.isEmpty() ? "NO_ACTIVITY" : "ACTIVE";

                log.debug("Preceptor {} billing: revenue={}, failed={}, transactions={}, status={}",
                        preceptor.getUserId(), successfulRevenue, failedRevenue,
                        allTransactions.size(), status);

                return new PreceptorBillingReportDTO(
                        preceptor.getUserId(),
                        preceptor.getUser().getDisplayName(),
                        preceptor.isVerified() ? "VERIFIED" : "PENDING",
                        preceptor.isPremium() ? "Premium" : "Standard",
                        successfulRevenue,
                        failedRevenue,
                        successCount,
                        lastInvoiceDate,
                        lastTransactionDate,
                        status,
                        null, // notes
                        failedCount,
                        allTransactions.size()
                );
            });

        } catch (Exception e) {
            log.error("Error fetching revenue by preceptor: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    @Override
    public org.springframework.core.io.Resource downloadLicenseAsAdmin(Long userId) {
        log.info("Admin downloading license for preceptor - userId: {}", userId);
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));
        if (preceptor.getLicenseFileUrl() == null || preceptor.getLicenseFileUrl().isEmpty()) {
            throw new ResourceNotFoundException("License file not found for preceptor with ID: " + userId);
        }
        try {
            Path path = Paths.get(preceptor.getLicenseFileUrl());
            org.springframework.core.io.FileSystemResource resource = new org.springframework.core.io.FileSystemResource(path.toFile());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("License file not accessible");
            }
            return resource;
        } catch (Exception e) {
            log.error("Error downloading license for preceptor {}: {}", userId, e.getMessage());
            throw new ResourceNotFoundException("Error downloading license: " + e.getMessage());
        }
    }

    @Override
    public org.springframework.core.io.Resource viewLicenseImageAsAdmin(Long userId) {
        log.info("Admin viewing license image for preceptor - userId: {}", userId);
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));
        if (preceptor.getLicenseFileUrl() == null || preceptor.getLicenseFileUrl().isEmpty()) {
            throw new ResourceNotFoundException("License file not found for preceptor with ID: " + userId);
        }
        try {
            Path path = Paths.get(preceptor.getLicenseFileUrl());
            org.springframework.core.io.FileSystemResource resource = new org.springframework.core.io.FileSystemResource(path.toFile());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("License file not accessible");
            }
            log.info("✓ License image retrieved for display: preceptor={}, file={}", userId, path.getFileName());
            return resource;
        } catch (Exception e) {
            log.error("Error viewing license image for preceptor {}: {}", userId, e.getMessage());
            throw new ResourceNotFoundException("Error retrieving license image: " + e.getMessage());
        }
    }

    // Helper methods
    private AdminPreceptorListDTO mapPreceptorToListDTO(Preceptor preceptor) {
        return new AdminPreceptorListDTO(
                preceptor.getUserId(),
                preceptor.getUser().getDisplayName(),
                preceptor.getSpecialty(),
                preceptor.getLocation(),
                preceptor.isVerified(),
                preceptor.isPremium(),
                preceptor.getVerificationStatus(),
                preceptor.getVerificationSubmittedAt(),
                preceptor.getVerificationReviewedAt(),
                preceptor.getCreatedAt(),
                preceptor.getLastModifiedAt()
        );
    }

    private AdminPreceptorDetailDTO mapPreceptorToDetailDTO(Preceptor preceptor) {
        return new AdminPreceptorDetailDTO(
                preceptor.getUserId(),
                preceptor.getUser().getDisplayName(),
                preceptor.getEmail(),
                preceptor.getPhone(),
                preceptor.getCredentials(),
                preceptor.getSpecialty(),
                preceptor.getLocation(),
                preceptor.getSetting(),
                preceptor.getAvailableDays() != null
                        ? preceptor.getAvailableDays().stream().map(Enum::name).collect(Collectors.toList())
                        : new ArrayList<>(),
                preceptor.getHonorarium(),
                preceptor.getRequirements(),
                preceptor.isVerified(),
                preceptor.isPremium(),
                preceptor.getLicenseNumber(),
                preceptor.getLicenseState(),
                preceptor.getLicenseFileUrl(),
                preceptor.getVerificationStatus(),
                preceptor.getVerificationSubmittedAt(),
                preceptor.getVerificationReviewedAt(),
                new ArrayList<>(), // verification notes - placeholder
                new ArrayList<>(), // verification history - placeholder
                preceptor.getCreatedAt(),
                preceptor.getLastModifiedAt()
        );
    }

    private AdminStudentListDTO mapStudentToListDTO(Student student) {
        return new AdminStudentListDTO(
                student.getUserId(),
                student.getUser().getDisplayName(),
                student.getUser().getEmail(),
                student.getUniversity(),
                student.getProgram(),
                student.getGraduationYear(),
                student.getCreatedAt(),
                student.getLastModifiedAt()
        );
    }

    private AdminStudentDetailDTO mapStudentToDetailDTO(Student student) {
        // Count inquiries sent by this student
        Page<com.digitalearn.npaxis.inquiry.Inquiry> inquiriesPage = inquiryRepository
                .findByStudent_User_UserId(student.getUserId(), PageRequest.of(0, 1));
        int inquiriesSent = (int) inquiriesPage.getTotalElements();

        return new AdminStudentDetailDTO(
                student.getUserId(),
                student.getUser().getDisplayName(),
                student.getUser().getEmail(),
                student.getPhone(),
                student.getUniversity(),
                student.getProgram(),
                student.getGraduationYear(),
                student.getSavedPreceptors() != null ? student.getSavedPreceptors().size() : 0,
                inquiriesSent,
                null, // last activity - would need audit log
                student.getUser().isEmailVerified(),
                student.getUser().isAccountEnabled(),
                student.getCreatedAt(),
                student.getLastModifiedAt()
        );
    }
}
