package com.sunrise.dental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private long totalPatients;
    private long totalAppointments;
    private long todayAppointments;
    private long completedAppointmentsToday;
    private long upcomingAppointments;
    private long cancelledAppointments;
    private long activeDentists;
    private long pendingBills;
    private double revenueThisMonth;
    private double collectedThisMonth;
    private List<AppointmentResponse> todaySchedule;
    private List<Map<String, Object>> revenueByDay;
    private List<Map<String, Object>> appointmentStatusDistribution;
    private LocalDateTime generatedAt;
}
