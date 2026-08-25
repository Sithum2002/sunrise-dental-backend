package com.sunrise.dental.service.impl;

import com.sunrise.dental.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Email delivery via Spring's JavaMailSender. When SMTP is not configured
 * (e.g. dev sandbox), the service falls back to a simulated delivery that is
 * logged - this keeps the notification pipeline working end-to-end.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${app.mail.enabled:false}") boolean enabled,
                            @Value("${app.mail.from:Souvenir Dental <info@sunrisedental.lk>}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    @Override
    @Async("notificationExecutor")
    public boolean sendEmail(String to, String subject, String htmlBody) {
        if (!enabled) {
            log.info("[SIMULATED EMAIL] to={} subject={} body={}", to, subject,
                    htmlBody.length() > 200 ? htmlBody.substring(0, 200) + "..." : htmlBody);
            return true;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean isConfigured() {
        return enabled;
    }
}
