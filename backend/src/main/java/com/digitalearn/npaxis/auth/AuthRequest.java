package com.digitalearn.npaxis.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * DTO for authentication requests during login.
 */
@Data
public class AuthRequest {

    /**
     * Email of the user attempting to log in.
     */
    @NotBlank(message = "Email must not be blank")
    private String email;

    /**
     * User password (excluded from toString to avoid logging sensitive data).
     */
    @NotBlank(message = "Password must not be blank")
    @ToString.Exclude
    private String password;
}
