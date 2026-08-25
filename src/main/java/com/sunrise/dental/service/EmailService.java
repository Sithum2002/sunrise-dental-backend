package com.sunrise.dental.service;

/**
 * Email delivery contract (Strategy pattern - pluggable transport).
 */
public interface EmailService {

    boolean sendEmail(String to, String subject, String htmlBody);

    boolean isConfigured();
}
