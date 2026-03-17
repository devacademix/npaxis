package com.digitalearn.npaxis.admin;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO representing the response returned after a successful authentication.
 */
@Getter
@Builder
public class AdminRegisterResponse {
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
     * Set of roles assigned to the authenticated user.
     */
    private String role;
}
