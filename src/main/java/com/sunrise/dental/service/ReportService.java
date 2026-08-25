package com.sunrise.dental.service;

import com.sunrise.dental.enums.ReportFormat;
import com.sunrise.dental.enums.ReportType;

import java.util.Map;

/**
 * Report generation contract. Data preparation is delegated to strategies
 * (report type -> dataset) and rendering to the Jasper engine (PDF/CSV).
 */
public interface ReportService {

    GeneratedReport generate(ReportType type, ReportFormat format, Map<String, Object> params);

    /**
     * Rendered report payload.
     */
    record GeneratedReport(byte[] content, String contentType, String fileName) {
    }
}
