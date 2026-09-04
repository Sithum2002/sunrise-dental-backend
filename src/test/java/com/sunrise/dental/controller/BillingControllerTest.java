package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.BillRequest;
import com.sunrise.dental.dto.request.PaymentRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.BillResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.PaymentResponse;
import com.sunrise.dental.enums.PaymentMethod;
import com.sunrise.dental.enums.PaymentStatus;
import com.sunrise.dental.service.BillingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    @Mock
    private BillingService billingService;

    @InjectMocks
    private BillingController billingController;

    private BillResponse billResponse() {
        return new BillResponse(
                1L, "INV-0001", 1L, "AP-2026-0001", 1L, "John Doe",
                2L, "Jane Smith", "Dental Cleaning", "TRT-CLEAN",
                LocalDateTime.now(), 5000.0, 1500.0, 0.0, 650.0,
                7150.0, 0.0, 7150.0, PaymentStatus.UNPAID, null, LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/billing")
    class CreateBill {

        @Test
        @DisplayName("creates a bill")
        void createBill_success() {
            BillRequest request = BillRequest.builder().appointmentId(1L).build();
            when(billingService.createBill(request)).thenReturn(billResponse());

            ResponseEntity<ApiResponse<BillResponse>> result = billingController.createBill(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Bill generated", result.getBody().getMessage());
            assertEquals("INV-0001", result.getBody().getData().billNumber());
            verify(billingService).createBill(request);
        }
    }

    @Nested
    @DisplayName("GET /api/billing")
    class GetAll {

        @Test
        @DisplayName("returns paginated bills")
        void getAll_success() {
            PageResponse<BillResponse> pageResponse = new PageResponse<>(
                    List.of(billResponse()), 0, 10, 1, 1);
            when(billingService.getAll(any(), any(), any(), any(), any()))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<BillResponse>>> result =
                    billingController.getAll(null, null, null, null, PageRequest.of(0, 10));

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().content().size());
        }

        @Test
        @DisplayName("returns empty page when no bills")
        void getAll_empty() {
            PageResponse<BillResponse> pageResponse = new PageResponse<>(
                    Collections.emptyList(), 0, 10, 0, 0);
            when(billingService.getAll(any(), any(), any(), any(), any())).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<BillResponse>>> result =
                    billingController.getAll(null, null, null, null, PageRequest.of(0, 10));

            assertTrue(result.getBody().getData().content().isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/billing/{id}")
    class GetById {

        @Test
        @DisplayName("returns bill by id")
        void getById_success() {
            when(billingService.getById(1L)).thenReturn(billResponse());

            ResponseEntity<ApiResponse<BillResponse>> result = billingController.getById(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("INV-0001", result.getBody().getData().billNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/billing/number/{billNumber}")
    class GetByNumber {

        @Test
        @DisplayName("returns bill by number")
        void getByNumber_success() {
            when(billingService.getByBillNumber("INV-0001")).thenReturn(billResponse());

            ResponseEntity<ApiResponse<BillResponse>> result = billingController.getByNumber("INV-0001");

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Bill retrieved", result.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("GET /api/billing/patient/{patientId}")
    class GetPatientBills {

        @Test
        @DisplayName("returns patient bills")
        void getPatientBills_success() {
            Pageable pageable = PageRequest.of(0, 10);
            PageResponse<BillResponse> pageResponse = new PageResponse<>(
                    List.of(billResponse()), 0, 10, 1, 1);
            when(billingService.getPatientBills(1L, pageable)).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<BillResponse>>> result =
                    billingController.getPatientBills(1L, pageable);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().content().size());
        }
    }

    @Nested
    @DisplayName("GET /api/billing/{billId}/payments")
    class GetPayments {

        @Test
        @DisplayName("returns payments for bill")
        void getPayments_success() {
            PaymentResponse paymentResponse = new PaymentResponse(
                    1L, 1L, "INV-0001", 5000.0, PaymentMethod.CASH,
                    null, LocalDateTime.now(), null);
            when(billingService.getPaymentsForBill(1L)).thenReturn(List.of(paymentResponse));

            ResponseEntity<ApiResponse<List<PaymentResponse>>> result =
                    billingController.getPayments(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().size());
        }

        @Test
        @DisplayName("returns empty list when no payments")
        void getPayments_empty() {
            when(billingService.getPaymentsForBill(1L)).thenReturn(Collections.emptyList());

            ResponseEntity<ApiResponse<List<PaymentResponse>>> result =
                    billingController.getPayments(1L);

            assertTrue(result.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/billing/payments")
    class RecordPayment {

        @Test
        @DisplayName("records a payment")
        void recordPayment_success() {
            PaymentRequest request = PaymentRequest.builder()
                    .billId(1L).amount(5000.0).paymentMethod(PaymentMethod.CASH)
                    .build();
            PaymentResponse paymentResponse = new PaymentResponse(
                    1L, 1L, "INV-0001", 5000.0, PaymentMethod.CASH,
                    null, LocalDateTime.now(), null);
            when(billingService.recordPayment(request)).thenReturn(paymentResponse);

            ResponseEntity<ApiResponse<PaymentResponse>> result =
                    billingController.recordPayment(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Payment recorded", result.getBody().getMessage());
            assertEquals(5000.0, result.getBody().getData().amount());
        }
    }
}
