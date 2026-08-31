package com.sunrise.dental.dto.response;

import com.sunrise.dental.enums.PaymentMethod;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long billId,
        String billNumber,
        Double amount,
        PaymentMethod paymentMethod,
        String referenceNo,
        LocalDateTime paymentDate,
        String remarks
) {
}
