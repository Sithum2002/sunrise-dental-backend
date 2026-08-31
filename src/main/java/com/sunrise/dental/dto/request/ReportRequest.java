package com.sunrise.dental.dto.request;

import com.sunrise.dental.enums.ReportFormat;
import com.sunrise.dental.enums.ReportType;
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
public class ReportRequest {

    @NotNull(message = "Report type is required")
    private ReportType type;

    @NotNull(message = "Report format is required")
    private ReportFormat format;

    private Long entityId;

    private String startDate;

    private String endDate;
}
