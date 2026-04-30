package com.digitalearn.npaxis.email;

import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.util.Map;

public interface EmailService {
    @Async
    void sendEmail(String to, EmailTemplate template, Map<String, Object> templateModel);

    @Async
    void sendEmailWithAttachment(String to, EmailTemplate template, Map<String, Object> templateModel, File attachment, String attachmentFileName);
}