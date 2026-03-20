package com.digitalearn.npaxis.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplate {
    
    EMAIL_VERIFICATION("email-verification","Verify Your NPaxis Account"),
    WELCOME_EMAIL("welcome-email","Welcome to NPaxis Platform");

    private final String templateName;
    private final String subject;
}