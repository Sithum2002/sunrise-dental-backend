package com.sunrise.dental.service;

/**
 * SMS delivery contract (Strategy pattern - pluggable gateway).
 */
public interface SmsService {

    boolean sendSms(String phoneNumber, String message);

    boolean isConfigured();
}
