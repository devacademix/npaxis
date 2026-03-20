package com.digitalearn.npaxis.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplate {

    EMAIL_VERIFICATION("email-verification", "Verify Your NPaxis Account"),
    WELCOME_EMAIL("welcome-email", "Welcome to NPaxis Platform"),
    FORGOT_PASSWORD("forgot-password", "Request to Reset your password.");

    private final String templateName;
    private final String subject;
}