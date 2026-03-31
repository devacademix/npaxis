package com.digitalearn.npaxis.auth;

import com.digitalearn.npaxis.email.EmailService;
import com.digitalearn.npaxis.email.EmailTemplate;
import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;
import com.digitalearn.npaxis.exceptions.ResourceAlreadyExistsException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.role.Role;
import com.digitalearn.npaxis.role.RoleName;
import com.digitalearn.npaxis.role.RoleRepository;
import com.digitalearn.npaxis.security.jwt.JwtService;
import com.digitalearn.npaxis.token.TokenRepository;
import com.digitalearn.npaxis.token.TokenService;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation responsible for authentication,
 * user registration, token refresh, and logout.
 * <p>
 * Access token is returned in the response body.
 * Refresh token is stored securely in an HttpOnly cookie.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final TokenService tokenService;

    private final List<RegistrationStrategy> registrationStrategies;

    /**
     * Authenticates user credentials and generates JWT tokens.
     *
     * @param authRequest     login credentials
     * @param servletResponse HTTP response to attach refresh cookie
     * @return AuthResponse containing access token and user info
     */
    @Override
    public AuthResponse login(AuthRequest authRequest, HttpServletResponse servletResponse)
            throws AuthenticationException {

        log.info("Login attempt for email '{}'", authRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            return buildAuthResponse(user, servletResponse);

        } catch (DisabledException _) {

            User user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow();

            if (!user.isEmailVerified()) {
                sendValidationEmail(user, EmailTemplate.EMAIL_VERIFICATION);
                throw new BusinessException(BusinessErrorCodes.EMAIL_NOT_VERIFIED);
            }

            throw new BusinessException(BusinessErrorCodes.ACCOUNT_DISABLED);

        } catch (LockedException _) {
            throw new BusinessException(BusinessErrorCodes.ACCOUNT_LOCKED);
        }
    }

    /**
     * Registers a new user and creates a corresponding Student or Preceptor entity.
     * <p>
     * After registration, tokens are issued just like login.
     *
     * @param request registration request
     * @return AuthResponse with access token
     */
    @Override
    @Transactional
    public String register(BaseRegistrationRequest request) {
        log.info("Registration started for email '{}'", request.getEmail());

        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User already exists with Email: " + request.getEmail());
        }

        // 2. Resolve role by logical type (student/preceptor) instead of brittle DB IDs
        Role role = resolveRegistrationRole(request.getRoleId());

        // 3. Create and Save the Base User
        User user = User.builder()
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .role(role)

                .build();

        User savedUser = userRepository.save(user);

        // 4. Find the strategy and create the role-specific profile
        RegistrationStrategy strategy = registrationStrategies.stream()
                .filter(s -> s.supports(request.getRoleId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID provided"));

        strategy.createProfile(savedUser, request);

        log.info("User '{}' registered successfully with ID {}", savedUser.getEmail(), savedUser.getUserId());

        // 5. Send Validation Email
        this.sendValidationEmail(savedUser, EmailTemplate.EMAIL_VERIFICATION);

        // 6. Build and return the response
        return "User registered successfully. Please check your email for verification.";
    }

    private Role resolveRegistrationRole(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("roleId is required for registration.");
        }

        RoleName roleName = switch (roleId.intValue()) {
            case 1 -> RoleName.ROLE_STUDENT;
            case 2 -> RoleName.ROLE_PRECEPTOR;
            default -> throw new IllegalArgumentException("Invalid role ID provided. Expected 1 (Student) or 2 (Preceptor).");
        };

        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    log.warn("Role '{}' not found in database. Auto-creating it during registration.", roleName);
                    return roleRepository.save(Role.builder().roleName(roleName).build());
                });
    }

    private void sendValidationEmail(User user, EmailTemplate emailTemplate) {
        String plainOtp = tokenService.generateAndSaveToken(user.getEmail());

        emailService.sendEmail(
                user.getEmail(),
                emailTemplate,
                Map.of(
                        "name", user.getDisplayName(),
                        "otp", plainOtp
                )
        );
    }


    @Transactional
    @Override
    public AuthResponse verifyEmail(String email, String otp, HttpServletResponse servletResponse) {
        log.info("Verifying email for user with email: " + email);
        // 1. Validate OTP
        boolean isValid = tokenService.verifyToken(email, otp);

        if (isValid) {
            // 2. Enable User
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            user.setAccountEnabled(true);
            user.setEmailVerified(true);
            userRepository.save(user);

            // 3. Send Successful onboarding email.
            emailService.sendEmail(
                    user.getEmail(),
                    EmailTemplate.WELCOME_EMAIL,
                    Map.of(
                            "name", user.getDisplayName()
                    )
            );

            log.info("User account enabled successfully");
            return this.buildAuthResponse(user, servletResponse);
        } else {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
    }

    /**
     * Generates a new access token using the refresh token.
     *
     * @param refreshToken refresh token extracted from a cookie
     * @return AuthResponse containing a new access token
     */
    @Override
    public AuthResponse refreshToken(String refreshToken) {

        log.info("Refreshing access token");

        String email = jwtService.extractUsernameFromToken(refreshToken);

        if (email == null) {
            throw new AuthenticationException("Invalid refresh token") {
            };
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new AuthenticationException("Invalid refresh token") {
            };
        }

        Map<String, Object> claims = createClaims(user);

        String accessToken = jwtService.generateAccessToken(claims, user);

        return AuthResponse.builder()
                .userId(user.getUserId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .role(user.getRole().getRoleName().name())
                .accessToken(accessToken)
                .build();
    }

    /**
     * Logs out user by clearing refresh token cookie.
     *
     * @param servletResponse response object
     * @param refreshToken    current refresh token
     */
    @Override
    public void logout(HttpServletResponse servletResponse, String refreshToken) {

        log.info("Logout initiated");

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ZERO)
                .build();

        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmail(forgotPasswordRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + forgotPasswordRequest.email()));

        this.sendValidationEmail(user, EmailTemplate.FORGOT_PASSWORD);
        return "An OTP has been sent to the email: " + user.getEmail();
    }

    @Override
    public String resetPassword(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        this.userRepository.save(user);
        return "Password reset successfully. Please login.";
    }

    /**
     * Initializes system roles and default admin user.
     *
     * @return status message
     */
    @Override
    public String initializeAdmin() {

        log.info("Initializing system roles and admin user");

        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByRoleName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));
        }

        createUserIfNotExists("Super Admin", "admin@npaxis.com", "admin", RoleName.ROLE_ADMIN);

        return "Roles and Admin Initialized";
    }

    /**
     * Creates a user if not already present.
     */
    private void createUserIfNotExists(String name, String email, String password, RoleName roleName) {

        if (userRepository.existsByEmail(email)) {
            return;
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = User.builder()
                .displayName(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);

        log.info("Default admin '{}' created", email);
    }

    /**
     * Generates JWT claims.
     */
    private Map<String, Object> createClaims(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());

        return claims;
    }

    /**
     * Generates access and refresh tokens and sets refresh cookie.
     */
    private AuthResponse buildAuthResponse(User user, HttpServletResponse servletResponse) {

        Map<String, Object> claims = createClaims(user);

        String accessToken = jwtService.generateAccessToken(claims, user);
        String refreshToken = jwtService.generateRefreshToken(claims, user);

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .sameSite("None")
                        .maxAge(Duration.ofDays(7))
                        .build();

        servletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .role(user.getRole().getRoleName().name())
                .accessToken(accessToken)
                .build();
    }
}
