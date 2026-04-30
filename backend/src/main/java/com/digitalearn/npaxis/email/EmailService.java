package com.digitalearn.npaxis.email;

import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface EmailService {
    @Async
    void sendEmail(String to, EmailTemplate template, Map<String, Object> templateModel);
}