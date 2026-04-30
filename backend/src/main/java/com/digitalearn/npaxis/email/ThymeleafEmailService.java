package com.digitalearn.npaxis.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThymeleafEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailConfigProperties emailConfig;

    @Async // Runs in a separate thread so the user doesn't wait for the email to send
    @Override
    public void sendEmail(String to, EmailTemplate emailTemplate, Map<String, Object> templateModel) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            String template = templateEngine.process(emailTemplate.getTemplateName(), thymeleafContext);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFromAddress(), emailConfig.getFromName());
            helper.setTo(to);
            helper.setSubject(emailTemplate.getSubject());
            helper.setText(template, true); // true indicates HTML

            this.mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Depending on requirements, you might want to save failed emails to a DLQ table here
        }
    }

    @Async
    @Override
    public void sendEmailWithAttachment(String to, EmailTemplate emailTemplate, Map<String, Object> templateModel, File attachment, String attachmentFileName) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            String template = templateEngine.process(emailTemplate.getTemplateName(), thymeleafContext);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFromAddress(), emailConfig.getFromName());
            helper.setTo(to);
            helper.setSubject(emailTemplate.getSubject());
            helper.setText(template, true); // true indicates HTML

            // Add attachment
            if (attachment != null && attachment.exists()) {
                helper.addAttachment(attachmentFileName, attachment);
            }

            this.mailSender.send(message);
            log.info("Email with attachment sent successfully to: {}", to);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email with attachment to {}: {}", to, e.getMessage());
        }
    }
}