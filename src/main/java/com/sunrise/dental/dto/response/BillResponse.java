package com.sunrise.dental.dto.response;

import com.sunrise.dental.enums.PaymentMethod;
import com.sunrise.dental.enums.PaymentStatus;

import java.time.LocalDateTime;

public record BillResponse(
        Long id,
        String billNumber,
        Long appointmentId,
        String appointmentNumber,
        Long patientId,
        String patientName,
        Long dentistId,
        String dentistName,
        String treatmentName,
        String treatmentCode,
        LocalDateTime appointmentDate,
        Double treatmentCost,
        Double consultationFee,
        Double discount,
        Double tax,
        Double totalAmount,
        Double amountPaid,
        Double dueAmount,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        LocalDateTime billedAt
) {
}
