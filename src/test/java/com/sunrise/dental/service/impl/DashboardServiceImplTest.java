package com.sunrise.dental.service.impl;

import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.DashboardStatsResponse;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Dentist;
import com.sunrise.dental.entity.Patient;
import com.sunrise.dental.entity.Treatment;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.enums.Gender;
import com.sunrise.dental.enums.PaymentStatus;
import com.sunrise.dental.mapper.AppointmentMapper;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.BillRepository;
import com.sunrise.dental.repository.DentistRepository;
import com.sunrise.dental.repository.PatientRepository;
import com.sunrise.dental.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private DentistRepository dentistRepository;
    @Mock
    private TreatmentRepository treatmentRepository;
    @Mock
    private AppointmentMapper appointmentMapper;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        Patient patient = Patient.builder()
                .id(1L)
                .firstName("John").lastName("Doe")
                .contactNumber("0771234567").email("j@e.com")
                .gender(Gender.MALE).active(true)
                .build();
        Dentist dentist = Dentist.builder()
                .id(1L).firstName("Jane").lastName("Smith")
                .build();
        Treatment treatment = Treatment.builder()
                .id(1L).name("Cleaning").code("TRT-C")
                .build();
        appointment = Appointment.builder()
                .id(1L).appointmentNumber("AP-2026-0001")
                .patient(patient).dentist(dentist).treatment(treatment)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }

    @Nested
    @DisplayName("getStats()")
    class GetStats {

        @Test
        @DisplayName("returns all dashboard statistics")
        void getStats_success() {
            LocalDate today = LocalDate.now();
            YearMonth month = YearMonth.now();

            when(appointmentRepository.findByAppointmentDate(today)).thenReturn(List.of(appointment));
            when(appointmentRepository.findUpcomingToday(eq(today), any(LocalTime.class))).thenReturn(List.of(appointment));
            when(appointmentRepository.count()).thenReturn(100L);
            when(appointmentRepository.countByAppointmentDate(today)).thenReturn(5L);
            when(appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.COMPLETED)).thenReturn(2L);
            when(appointmentRepository.countByStatus(any(AppointmentStatus.class))).thenReturn(0L);
            when(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED)).thenReturn(3L);
            when(patientRepository.countByActiveTrue()).thenReturn(50L);
            when(dentistRepository.countByStatus(com.sunrise.dental.enums.DentistStatus.AVAILABLE)).thenReturn(4L);
            when(billRepository.countByPaymentStatus(PaymentStatus.PARTIAL)).thenReturn(1L);
            when(billRepository.countByPaymentStatus(PaymentStatus.UNPAID)).thenReturn(2L);
            when(billRepository.sumTotalBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(100000.0);
            when(billRepository.sumPaidBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(75000.0);
            when(appointmentMapper.toResponse(any(Appointment.class)))
                    .thenReturn(new AppointmentResponse(1L, "AP-2026-0001", 1L, "John Doe", "0771234567",
                            1L, "Jane Smith", 1L, "Cleaning", "TRT-C",
                            today, LocalTime.of(9, 0), LocalTime.of(9, 30),
                            AppointmentStatus.SCHEDULED, null, null, null));

            DashboardStatsResponse result = dashboardService.getStats();

            assertNotNull(result);
            assertEquals(50L, result.getTotalPatients());
            assertEquals(100L, result.getTotalAppointments());
            assertEquals(5L, result.getTodayAppointments());
            assertEquals(2L, result.getCompletedAppointmentsToday());
            assertEquals(1L, result.getUpcomingAppointments());
            assertEquals(3L, result.getCancelledAppointments());
            assertEquals(4L, result.getActiveDentists());
            assertEquals(3L, result.getPendingBills());
            assertEquals(100000.0, result.getRevenueThisMonth());
            assertEquals(75000.0, result.getCollectedThisMonth());
            assertEquals(1, result.getTodaySchedule().size());
            assertNotNull(result.getAppointmentStatusDistribution());
            assertNotNull(result.getGeneratedAt());
        }

        @Test
        @DisplayName("returns zero stats when system is empty")
        void getStats_empty() {
            LocalDate today = LocalDate.now();
            when(appointmentRepository.findByAppointmentDate(today)).thenReturn(List.of());
            when(appointmentRepository.findUpcomingToday(eq(today), any(LocalTime.class))).thenReturn(List.of());
            when(appointmentRepository.count()).thenReturn(0L);
            when(appointmentRepository.countByAppointmentDate(today)).thenReturn(0L);
            when(appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.COMPLETED)).thenReturn(0L);
            when(appointmentRepository.countByStatus(any(AppointmentStatus.class))).thenReturn(0L);
            when(patientRepository.countByActiveTrue()).thenReturn(0L);
            when(dentistRepository.countByStatus(com.sunrise.dental.enums.DentistStatus.AVAILABLE)).thenReturn(0L);
            when(billRepository.countByPaymentStatus(any(PaymentStatus.class))).thenReturn(0L);
            when(billRepository.sumTotalBetween(any(), any())).thenReturn(0.0);
            when(billRepository.sumPaidBetween(any(), any())).thenReturn(0.0);

            DashboardStatsResponse result = dashboardService.getStats();

            assertNotNull(result);
            assertEquals(0L, result.getTotalPatients());
            assertEquals(0L, result.getTotalAppointments());
            assertEquals(0.0, result.getRevenueThisMonth());
            assertTrue(result.getTodaySchedule().isEmpty());
        }

        @Test
        @DisplayName("returns empty revenueByDay on stored proc failure")
        void getStats_storedProcFails() {
            LocalDate today = LocalDate.now();
            when(appointmentRepository.findByAppointmentDate(today)).thenReturn(List.of());
            when(appointmentRepository.findUpcomingToday(eq(today), any(LocalTime.class))).thenReturn(List.of());
            when(appointmentRepository.count()).thenReturn(0L);
            when(appointmentRepository.countByAppointmentDate(today)).thenReturn(0L);
            when(appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.COMPLETED)).thenReturn(0L);
            when(appointmentRepository.countByStatus(any(AppointmentStatus.class))).thenReturn(0L);
            when(patientRepository.countByActiveTrue()).thenReturn(0L);
            when(dentistRepository.countByStatus(com.sunrise.dental.enums.DentistStatus.AVAILABLE)).thenReturn(0L);
            when(billRepository.countByPaymentStatus(any(PaymentStatus.class))).thenReturn(0L);
            when(billRepository.sumTotalBetween(any(), any())).thenReturn(0.0);
            when(billRepository.sumPaidBetween(any(), any())).thenReturn(0.0);
            when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                    .thenThrow(new RuntimeException("SP not found"));

            DashboardStatsResponse result = dashboardService.getStats();

            assertNotNull(result);
            assertTrue(result.getRevenueByDay().isEmpty());
        }
    }
}
