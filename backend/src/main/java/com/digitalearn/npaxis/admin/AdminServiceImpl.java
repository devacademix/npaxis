package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.exceptions.ResourceAlreadyExistsException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.preceptor.VerificationStatus;
import com.digitalearn.npaxis.role.Role;
import com.digitalearn.npaxis.role.RoleName;
import com.digitalearn.npaxis.role.RoleRepository;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PreceptorRepository preceptorRepository;
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
}
