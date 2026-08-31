package com.sunrise.dental.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private Double discount;

    private String remarks;
}
