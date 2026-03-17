package com.digitalearn.npaxis.auth;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO representing the response returned after a successful authentication.
 */
@Getter
@Builder
public class AuthResponse {
    /**
     * Unique identifier of the authenticated user.
     */
    private Long userId;

    /**
     * Full name of the authenticated user.
     */
    private String displayName;

    /**
     * Email address of the authenticated user.
     */
    private String email;

    /**
     * JWT access token to be used for accessing protected endpoints.
     */
    private String accessToken;

    /**
     * Set of roles assigned to the authenticated user.
     */
    private String role;
}
