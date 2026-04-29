package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.*;
import com.digitalearn.npaxis.analytics.AnalyticsEventRepository;
import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.exceptions.ResourceAlreadyExistsException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.preceptor.PreceptorResponseDTO;
import com.digitalearn.npaxis.preceptor.VerificationStatus;
import com.digitalearn.npaxis.role.Role;
import com.digitalearn.npaxis.role.RoleName;
import com.digitalearn.npaxis.role.RoleRepository;
import com.digitalearn.npaxis.student.Student;
import com.digitalearn.npaxis.student.StudentRepository;
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
        // Placeholder - would need a separate VerificationAuditLog table
        // For now, return empty list with basic audit info
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public String addVerificationNote(Long userId, String note, String noteType) {
        log.info("Adding verification note for preceptor - userId: {}, noteType: {}", userId, noteType);
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));
        // Placeholder - would need to store notes in a separate table
        log.info("Verification note added successfully");
        return "Note added successfully";
    }

    @Override
    public PreceptorBillingReportDTO getPreceptorBillingReport(Long userId) {
        log.info("Fetching billing report for preceptor - userId: {}", userId);
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));
        // Placeholder - would need to integrate with Stripe/subscription service
        return new PreceptorBillingReportDTO(
                userId,
                preceptor.getUser().getDisplayName(),
                "ACTIVE",
                "Premium",
                0.0,
                0.0,
                0,
                null,
                null,
                "PENDING",
                null,
                0,
                0
        );
    }

    @Override
    public PreceptorAnalyticsDTO getPreceptorAnalytics(Long userId) {
        log.info("Fetching analytics for preceptor - userId: {}", userId);
        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));
        // Placeholder - would integrate with analytics service
        return new PreceptorAnalyticsDTO(
                userId,
                preceptor.getUser().getDisplayName(),
                0L,
                0L,
                0L,
                0.0,
                0,
                0.0,
                0L,
                "STABLE",
                0.0
        );
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
    public List<SystemSettingsDTO> getAllSettings() {
        log.info("Fetching all system settings");
        // Placeholder - would need SystemSettings table/service
        return new ArrayList<>();
    }

    @Override
    public SystemSettingsDTO getSetting(String key) {
        log.info("Fetching setting - key: {}", key);
        // Placeholder
        return new SystemSettingsDTO(key, "", "", "GENERAL");
    }

    @Override
    @Transactional
    public SystemSettingsDTO updateSetting(String key, Object value) {
        log.info("Updating setting - key: {}, value: {}", key, value);
        // Placeholder - would need to persist settings
        return new SystemSettingsDTO(key, value, "", "GENERAL");
    }

    @Override
    public RevenueReportDTO getRevenueReport() {
        log.info("Fetching revenue report");
        // Placeholder - would integrate with Stripe API
        return new RevenueReportDTO(0.0, 0.0, 0.0, 0L, 0L, 0, 0.0, LocalDateTime.now());
    }

    @Override
    public Page<TransactionHistoryDTO> getTransactionHistory(Pageable pageable) {
        log.info("Fetching transaction history");
        // Placeholder - would integrate with Stripe API
        return Page.empty(pageable);
    }

    @Override
    public Page<PreceptorBillingReportDTO> getRevenueByPreceptor(Pageable pageable) {
        log.info("Fetching revenue by preceptor");
        Page<Preceptor> preceptors = preceptorRepository.findAllActive(pageable);
        return preceptors.map(p -> new PreceptorBillingReportDTO(
                p.getUserId(),
                p.getUser().getDisplayName(),
                "ACTIVE",
                "Premium",
                0.0,
                0.0,
                0,
                null,
                null,
                "PENDING",
                null,
                0,
                0
        ));
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
        return new AdminStudentDetailDTO(
                student.getUserId(),
                student.getUser().getDisplayName(),
                student.getUser().getEmail(),
                student.getPhone(),
                student.getUniversity(),
                student.getProgram(),
                student.getGraduationYear(),
                student.getSavedPreceptors() != null ? student.getSavedPreceptors().size() : 0,
                0, // inquires sent count - would need to query
                null, // last activity - would need audit log
                student.getUser().isEmailVerified(),
                student.getUser().isAccountEnabled(),
                student.getCreatedAt(),
                student.getLastModifiedAt()
        );
    }
}
