package com.sunrise.dental.service.impl;

import com.sunrise.dental.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Nested
    @DisplayName("When email is disabled")
    class Disabled {

        @Test
        @DisplayName("sendEmail simulates success without sending")
        void sendEmail_disabled() {
            EmailService service = new EmailServiceImpl(mailSender, false, "test@test.com");

            boolean result = service.sendEmail("to@test.com", "Subject", "<p>Body</p>");

            assertTrue(result);
            verify(mailSender, never()).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("isConfigured returns false")
        void isConfigured_disabled() {
            EmailService service = new EmailServiceImpl(mailSender, false, "test@test.com");

            assertFalse(service.isConfigured());
        }
    }

    @Nested
    @DisplayName("When email is enabled")
    class Enabled {

        @Test
        @DisplayName("sendEmail sends a MIME message")
        void sendEmail_enabled() {
            MimeMessage message = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(message);

            EmailService service = new EmailServiceImpl(mailSender, true, "Clinic <info@clinic.lk>");

            boolean result = service.sendEmail("to@test.com", "Subject", "<p>Body</p>");

            assertTrue(result);
            verify(mailSender).send(message);
        }

        @Test
        @DisplayName("returns false when sending fails")
        void sendEmail_sendFailure() {
            MimeMessage message = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(message);
            doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

            EmailService service = new EmailServiceImpl(mailSender, true, "Clinic <info@clinic.lk>");

            boolean result = service.sendEmail("to@test.com", "Subject", "<p>Body</p>");

            assertFalse(result);
        }

        @Test
        @DisplayName("isConfigured returns true")
        void isConfigured_enabled() {
            EmailService service = new EmailServiceImpl(mailSender, true, "test@test.com");

            assertTrue(service.isConfigured());
        }
    }
}
