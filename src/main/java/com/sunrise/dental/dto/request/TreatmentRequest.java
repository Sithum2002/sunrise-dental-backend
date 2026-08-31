package com.sunrise.dental.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class TreatmentRequest {

    @NotBlank(message = "Treatment code is required")
    @Pattern(regexp = "^TRT-[A-Z0-9]{2,8}$", message = "Treatment code must follow the format TRT-XX")
    private String code;

    @NotBlank(message = "Treatment name is required")
    @Size(max = 150, message = "Treatment name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than zero")
    private Double cost;

    @Min(value = 5, message = "Duration must be at least 5 minutes")
    private int durationMinutes;

    private boolean active;
}
