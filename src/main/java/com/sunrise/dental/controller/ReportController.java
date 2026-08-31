package com.sunrise.dental.controller;

import com.sunrise.dental.enums.ReportFormat;
import com.sunrise.dental.enums.ReportType;
import com.sunrise.dental.service.ReportService;
import com.sunrise.dental.service.ReportService.GeneratedReport;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Report generation endpoints - returns Jasper-generated PDF/CSV streams.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Generate a Jasper report (PDF/CSV)")
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generate(
            @RequestParam ReportType type,
            @RequestParam(defaultValue = "PDF") ReportFormat format,
            @RequestParam(required = false) Long billId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        Map<String, Object> params = new HashMap<>();
        params.put("billId", billId);
        params.put("patientId", patientId);
        params.put("from", from);
        params.put("to", to);
        params.put("year", year);
        params.put("month", month);

        GeneratedReport report = reportService.generate(type, format, params);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + report.fileName() + "\"")
                .contentType(MediaType.parseMediaType(report.contentType()))
                .body(report.content());
    }
}
