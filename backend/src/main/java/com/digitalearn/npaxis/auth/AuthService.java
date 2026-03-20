package com.digitalearn.npaxis.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.security.core.AuthenticationException;

/**
 * Service interface for handling authentication-related operations
 * such as login, user retrieval, and system initialization.
 */
public interface AuthService {

    /**
     * Authenticates a user using the provided login credentials.
     *
     * @param authRequest     The authentication request containing username and password.
     * @param servletResponse The HTTP servlet response object.
     * @return AuthResponse containing user details and a JWT token upon successful authentication.
     * @throws AuthenticationException If the authentication fails due to invalid credentials.
     */
    AuthResponse login(AuthRequest authRequest, HttpServletResponse servletResponse) throws AuthenticationException;

    @Transactional
    AuthResponse verifyEmail(String email, String otp, HttpServletResponse servletResponse);

    /**
     * Refreshes the JWT token using the provided refresh token.
     *
     * @param refreshToken The refresh token to be used for generating a new access token.
     * @return AuthResponse containing the new access token and user details.
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Registers a new user (Student or Preceptor) in the system.
     *
     * @param registerRequest The registration details.
     * @return String containing registered user details.
     */
    String register(BaseRegistrationRequest registerRequest);

    /**
     * Initializes default roles and users in the system (e.g., ADMIN and USER roles).
     * Typically called during application startup or for seeding initial data.
     *
     * @return A message indicating success or failure of the initialization process.
     */
    String initializeAdmin();

    void logout(HttpServletResponse servletResponse, String refreshToken);
}
