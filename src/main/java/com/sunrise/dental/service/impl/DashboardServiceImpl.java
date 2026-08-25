package com.sunrise.dental.service.impl;

import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.DashboardStatsResponse;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.enums.PaymentStatus;
import com.sunrise.dental.mapper.AppointmentMapper;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.BillRepository;
import com.sunrise.dental.repository.DentistRepository;
import com.sunrise.dental.repository.PatientRepository;
import com.sunrise.dental.repository.TreatmentRepository;
import com.sunrise.dental.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates KPIs for the dashboard using repository counts plus the
 * monthly-revenue stored procedure (advanced database feature).
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<AppointmentResponse> todaySchedule = appointmentRepository.findByAppointmentDate(today).stream()
                .sorted(java.util.Comparator.comparing(a -> a.getStartTime()))
                .map(appointmentMapper::toResponse).toList();

        long upcoming = appointmentRepository.findUpcomingToday(today, java.time.LocalTime.now()).size();

        Map<String, Object> statusDistribution = new LinkedHashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            statusDistribution.put(status.name(), appointmentRepository.countByStatus(status));
        }

        return DashboardStatsResponse.builder()
                .totalPatients(patientRepository.countByActiveTrue())
                .totalAppointments(appointmentRepository.count())
                .todayAppointments(appointmentRepository.countByAppointmentDate(today))
                .completedAppointmentsToday(appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.COMPLETED))
                .upcomingAppointments(upcoming)
                .cancelledAppointments(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED))
                .activeDentists(dentistRepository.countByStatus(com.sunrise.dental.enums.DentistStatus.AVAILABLE))
                .pendingBills(billRepository.countByPaymentStatus(PaymentStatus.PARTIAL)
                        + billRepository.countByPaymentStatus(PaymentStatus.UNPAID))
                .revenueThisMonth(round(billRepository.sumTotalBetween(monthStart, now)))
                .collectedThisMonth(round(billRepository.sumPaidBetween(monthStart, now)))
                .todaySchedule(todaySchedule)
                .revenueByDay(getRevenueByDay(currentMonth))
                .appointmentStatusDistribution(statusDistribution.entrySet().stream()
                        .map(e -> Map.<String, Object>of("status", e.getKey(), "count", e.getValue()))
                        .toList())
                .generatedAt(now)
                .build();
    }

    private double round(double value) {
        return java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private List<Map<String, Object>> getRevenueByDay(YearMonth month) {
        try {
            List<Map<String, Object>> rows =
                    jdbcTemplate.queryForList("CALL sp_GenerateMonthlyRevenue(?, ?)", month.getYear(), month.getMonthValue());
            return rows.stream().map(row -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("day", row.get("day"));
                map.put("totalBilled", toDouble(row.get("total_billed")));
                map.put("collected", toDouble(row.get("collected")));
                map.put("transactions", row.get("transactions"));
                return map;
            }).toList();
        } catch (Exception ex) {
            // Fallback if stored procedure not available (e.g. non-MySQL test DB)
            return List.of();
        }
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        return ((Number) value).doubleValue();
    }
}
