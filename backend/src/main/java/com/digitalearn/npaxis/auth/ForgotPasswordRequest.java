package com.digitalearn.npaxis.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @Email(message = "Email is required")
        @NotBlank(message = "Email is required")
        String email
) {
}
