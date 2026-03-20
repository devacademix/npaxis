package com.digitalearn.npaxis.auth;

import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.digitalearn.npaxis.utils.APIConstants.ACTIVATE_ACCOUNT;
import static com.digitalearn.npaxis.utils.APIConstants.AUTH_API;
import static com.digitalearn.npaxis.utils.APIConstants.INITIALIZE_ROLE_AND_USER_API;
import static com.digitalearn.npaxis.utils.APIConstants.LOGIN_API;
import static com.digitalearn.npaxis.utils.APIConstants.LOGOUT_API;
import static com.digitalearn.npaxis.utils.APIConstants.REFRESH_TOKEN;
import static com.digitalearn.npaxis.utils.APIConstants.USER_REGISTRATION_API;

/**
 * Controller responsible for handling authentication-related operations.
 * Includes login, user fetching, and admin initialization of roles and users.
 */

@RestController
@RequestMapping(AUTH_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for login, user management, and system setup")
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user using email and password.
     *
     * @param authRequest Object containing email and password.
     * @return JWT token and user information.
     */
    @Operation(summary = "Login with email and password", description = "Returns JWT token and user details on successful authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors")
    })
    @PostMapping(value = {LOGIN_API, LOGIN_API + "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequest authRequest, HttpServletResponse servletResponse) {

        log.info("Login attempt initiated for user with login ID:: {}", authRequest.getEmail());
        AuthResponse response = this.authService.login(authRequest, servletResponse);
        log.debug("Login successful for user: {}", authRequest.getEmail());

        return ResponseHandler.generateResponse(response, "Login successful", true, HttpStatus.OK);
    }

    /**
     * Registers a new user in the system.
     *
     * @param request Object containing registration details.
     * @return Registered user information.
     */
    @Operation(summary = "Register a new user", description = "Creates a new user with the specified role (STUDENT or PRECEPTOR).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors or user already exists")
    })
    @PostMapping(value = {USER_REGISTRATION_API, USER_REGISTRATION_API + "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(@RequestBody BaseRegistrationRequest request) {
        String response = authService.register(request);
        return ResponseHandler.generateResponse(response, "User registered successfully.", true, HttpStatus.OK);
    }

    /**
     * Refreshes the JWT token using the provided refresh token.
     *
     * @return New access token and user information.
     */
    @Operation(summary = "Refresh JWT token", description = "Generates a new access token using the provided refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid refresh token"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors")
    })
    @PostMapping(value = {REFRESH_TOKEN, REFRESH_TOKEN + "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        AuthResponse response = this.authService.refreshToken(refreshToken);
        log.debug("Token refreshed successfully for user.");
        return ResponseHandler.generateResponse(response, "Token refreshed successfully", true, HttpStatus.OK);
    }


    /**
     * Initializes roles and default admin user. Used during initial system setup.
     *
     * @return Map of success status and info.
     */
    @Operation(summary = "Initialize roles and admin user", description = "Creates initial roles (e.g., ADMIN, DOCTOR etc) and a default admin user. Intended for one-time system setup.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Initialization successful"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Already initialized or not allowed")
    })
    @PostMapping(value = {INITIALIZE_ROLE_AND_USER_API, INITIALIZE_ROLE_AND_USER_API + "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> initialize() {
        log.warn("Initializing default roles and users. This should only be called during system setup.");

        String initResponse = authService.initializeAdmin();
        log.info("System initialization completed with default roles and users.");

        return ResponseHandler.generateResponse(initResponse, "Users and Roles initialization successful", true, HttpStatus.OK);
    }

    @Operation(summary = "Logout", description = "Invalidates the provided refresh token and generates a new access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid refresh token"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors")
    })
    @PostMapping(value = {LOGOUT_API, LOGOUT_API + "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletResponse servletResponse,
            @CookieValue("refreshToken") String refreshToken
    ) {
        log.info("Logout initiated.");
        this.authService.logout(servletResponse, refreshToken);
        return ResponseHandler.generateResponse(null, "Logout successful", true, HttpStatus.OK);
    }

    @PostMapping(ACTIVATE_ACCOUNT)
    public ResponseEntity<Map<String, Object>> confirm(@RequestBody VerifyOTPRequest verifyOTPRequest, HttpServletResponse servletResponse) {
        log.info("Inside activate account controller");
        AuthResponse response = authService.verifyEmail(verifyOTPRequest.email(), verifyOTPRequest.otp(), servletResponse);

        return ResponseHandler.generateResponse(response, "Account activated successfully", true, HttpStatus.OK);
    }
}
