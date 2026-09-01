package com.sunrise.dental.event;

import com.sunrise.dental.entity.Bill;

import java.time.LocalDateTime;

/**
 * Domain event published when a payment is recorded against a bill.
 */
public record PaymentReceivedEvent(Bill bill, Double amount, LocalDateTime occurredAt) {

    public PaymentReceivedEvent(Bill bill, Double amount) {
        this(bill, amount, LocalDateTime.now());
    }
}
