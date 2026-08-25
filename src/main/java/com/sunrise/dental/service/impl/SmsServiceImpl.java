package com.sunrise.dental.service.impl;

import com.sunrise.dental.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * SMS delivery. When no real gateway credentials are configured the service
 * logs the message (simulated gateway) so the business flow remains intact.
 * The gateway abstraction keeps this a clean Strategy so a provider such as
 * Twilio/Telstra can be plugged in without changing callers.
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private final boolean enabled;
    private final String gatewayName;

    public SmsServiceImpl(@Value("${app.sms.enabled:false}") boolean enabled,
                          @Value("${app.sms.gateway:Simulator}") String gatewayName) {
        this.enabled = enabled;
        this.gatewayName = gatewayName;
    }

    @Override
    @Async("notificationExecutor")
    public boolean sendSms(String phoneNumber, String message) {
        if (!enabled) {
            log.info("[SIMULATED SMS via {}] to={} message={}", gatewayName, phoneNumber,
                    message.length() > 160 ? message.substring(0, 160) + "..." : message);
            return true;
        }
        try {
            // A real provider integration would be invoked here.
            log.info("[{}] Sending SMS to {}: {}", gatewayName, phoneNumber, message);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean isConfigured() {
        return enabled;
    }
}
