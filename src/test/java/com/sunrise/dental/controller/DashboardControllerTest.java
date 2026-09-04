package com.sunrise.dental.controller;

import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.DashboardStatsResponse;
import com.sunrise.dental.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @Nested
    @DisplayName("GET /api/dashboard/stats")
    class Stats {

        @Test
        @DisplayName("returns dashboard statistics")
        void stats_success() {
            DashboardStatsResponse stats = DashboardStatsResponse.builder()
                    .totalPatients(50L)
                    .totalAppointments(100L)
                    .todayAppointments(5L)
                    .completedAppointmentsToday(2L)
                    .upcomingAppointments(3L)
                    .cancelledAppointments(1L)
                    .activeDentists(4L)
                    .pendingBills(2L)
                    .revenueThisMonth(100000.0)
                    .collectedThisMonth(75000.0)
                    .todaySchedule(List.of())
                    .revenueByDay(List.of())
                    .appointmentStatusDistribution(List.of(
                            Map.of("status", "SCHEDULED", "count", 10)))
                    .generatedAt(LocalDateTime.now())
                    .build();
            when(dashboardService.getStats()).thenReturn(stats);

            ResponseEntity<ApiResponse<DashboardStatsResponse>> result = dashboardController.stats();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Dashboard statistics", result.getBody().getMessage());
            assertEquals(50L, result.getBody().getData().getTotalPatients());
            assertEquals(100000.0, result.getBody().getData().getRevenueThisMonth());
            assertEquals(1, result.getBody().getData().getAppointmentStatusDistribution().size());
        }
    }
}
