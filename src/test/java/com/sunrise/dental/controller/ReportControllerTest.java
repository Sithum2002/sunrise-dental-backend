package com.sunrise.dental.controller;

import com.sunrise.dental.enums.ReportFormat;
import com.sunrise.dental.enums.ReportType;
import com.sunrise.dental.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @Nested
    @DisplayName("GET /api/reports/generate")
    class Generate {

        @Test
        @DisplayName("generates a PDF report")
        void generate_pdf() {
            byte[] content = new byte[]{1, 2, 3, 4};
            ReportService.GeneratedReport generatedReport =
                    new ReportService.GeneratedReport(content, "application/pdf", "patient_list_2026-01-01.pdf");
            when(reportService.generate(eq(ReportType.PATIENT_LIST), eq(ReportFormat.PDF), any(Map.class)))
                    .thenReturn(generatedReport);

            ResponseEntity<byte[]> result = reportController.generate(
                    ReportType.PATIENT_LIST, ReportFormat.PDF, null, null,
                    null, null, null, null);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("application/pdf", result.getHeaders().getContentType().toString());
            assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0).contains("patient_list_2026-01-01.pdf"));
            assertArrayEquals(content, result.getBody());
            verify(reportService).generate(eq(ReportType.PATIENT_LIST), eq(ReportFormat.PDF), any(Map.class));
        }

        @Test
        @DisplayName("generates a CSV report")
        void generate_csv() {
            byte[] content = new byte[]{'a', 'b', 'c'};
            ReportService.GeneratedReport generatedReport =
                    new ReportService.GeneratedReport(content, "text/csv", "appointment_list_2026-01-01.csv");
            when(reportService.generate(eq(ReportType.APPOINTMENT_LIST), eq(ReportFormat.CSV), any(Map.class)))
                    .thenReturn(generatedReport);

            ResponseEntity<byte[]> result = reportController.generate(
                    ReportType.APPOINTMENT_LIST, ReportFormat.CSV, null, null,
                    null, null, null, null);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertArrayEquals(content, result.getBody());
        }

        @Test
        @DisplayName("passes query parameters to the report service")
        void generate_withParams() {
            ReportService.GeneratedReport generatedReport =
                    new ReportService.GeneratedReport(new byte[]{1}, "application/pdf", "bill_2026.pdf");
            when(reportService.generate(eq(ReportType.PATIENT_BILL), eq(ReportFormat.PDF), any(Map.class)))
                    .thenReturn(generatedReport);

            reportController.generate(ReportType.PATIENT_BILL, ReportFormat.PDF, 7L, 3L,
                    "2026-01-01", "2026-01-31", 2026, 1);

            verify(reportService).generate(eq(ReportType.PATIENT_BILL), eq(ReportFormat.PDF),
                    argThat(params -> params != null
                            && params.containsKey("billId")
                            && params.containsKey("patientId")
                            && params.containsKey("from")
                            && params.containsKey("to")
                            && params.containsKey("year")
                            && params.containsKey("month")));
        }
    }
}
