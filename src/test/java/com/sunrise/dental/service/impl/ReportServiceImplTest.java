package com.sunrise.dental.service.impl;

import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.BillRepository;
import com.sunrise.dental.repository.PatientRepository;
import com.sunrise.dental.repository.TreatmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private TreatmentRepository treatmentRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Nested
    @DisplayName("generate() - Loading report templates")
    class Generate {

        @Test
        @DisplayName("throws IllegalStateException when report template not found")
        void generate_missingTemplate() {
            assertThrows(IllegalStateException.class,
                    () -> reportService.generate(
                            com.sunrise.dental.enums.ReportType.PATIENT_BILL,
                            com.sunrise.dental.enums.ReportFormat.PDF,
                            java.util.Map.of()));
        }
    }

    @Nested
    @DisplayName("Report format metadata")
    class FormatMetadata {

        @Test
        @DisplayName("PDF format has correct content type and extension")
        void pdfMetadata() {
            assertEquals("application/pdf", com.sunrise.dental.enums.ReportFormat.PDF.getContentType());
            assertEquals("pdf", com.sunrise.dental.enums.ReportFormat.PDF.getExtension());
        }

        @Test
        @DisplayName("CSV format has correct content type and extension")
        void csvMetadata() {
            assertEquals("text/csv", com.sunrise.dental.enums.ReportFormat.CSV.getContentType());
            assertEquals("csv", com.sunrise.dental.enums.ReportFormat.CSV.getExtension());
        }
    }

    @Nested
    @DisplayName("ReportType labels")
    class ReportTypeLabels {

        @Test
        @DisplayName("all report types have non-empty labels")
        void labels() {
            for (com.sunrise.dental.enums.ReportType type : com.sunrise.dental.enums.ReportType.values()) {
                assertNotNull(type.getLabel());
                assertFalse(type.getLabel().isBlank());
            }
        }

        @Test
        @DisplayName("has 8 distinct report types")
        void count() {
            assertEquals(8, com.sunrise.dental.enums.ReportType.values().length);
        }
    }
}
