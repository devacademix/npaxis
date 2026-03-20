package com.digitalearn.npaxis.auth;

public record VerifyOTPRequest(
        String email,
        String otp
) {
}
