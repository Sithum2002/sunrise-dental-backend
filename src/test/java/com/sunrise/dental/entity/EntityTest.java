package com.sunrise.dental.entity;

import com.sunrise.dental.audit.AuditLog;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.enums.BloodGroup;
import com.sunrise.dental.enums.DentistStatus;
import com.sunrise.dental.enums.Gender;
import com.sunrise.dental.enums.NotificationChannel;
import com.sunrise.dental.enums.NotificationStatus;
import com.sunrise.dental.enums.PaymentMethod;
import com.sunrise.dental.enums.PaymentStatus;
import com.sunrise.dental.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Nested
    @DisplayName("Patient entity")
    class PatientEntity {

        @Test
        @DisplayName("getFullName concatenates first and last names")
        void getFullName() {
            Patient patient = Patient.builder()
                    .firstName("John").lastName("Doe")
                    .build();

            assertEquals("John Doe", patient.getFullName());
        }

        @Test
        @DisplayName("builder sets all fields correctly")
        void builder() {
            LocalDate dob = LocalDate.of(1990, 1, 1);
            Patient patient = Patient.builder()
                    .id(1L).regNo("SD-P0001").firstName("John").lastName("Doe")
                    .address("123 Main").contactNumber("0771234567")
                    .email("john@example.com").dateOfBirth(dob)
                    .gender(Gender.MALE).bloodGroup(BloodGroup.O_POSITIVE)
                    .allergies("Pollen").medicalHistory("None")
                    .emergencyContact("0777654321").active(true)
                    .build();

            assertEquals(1L, patient.getId());
            assertEquals("SD-P0001", patient.getRegNo());
            assertEquals(dob, patient.getDateOfBirth());
            assertEquals(Gender.MALE, patient.getGender());
            assertEquals(BloodGroup.O_POSITIVE, patient.getBloodGroup());
            assertTrue(patient.isActive());
        }

        @Test
        @DisplayName("all-args constructor works")
        void allArgsConstructor() {
            Patient patient = new Patient(
                    1L, "SD-P0001", "John", "Doe", "addr", "0771",
                    "e@e.com", LocalDate.of(1990, 1, 1), Gender.MALE,
                    BloodGroup.O_POSITIVE, "Pollen", "None", "0779", true);

            assertEquals("John Doe", patient.getFullName());
            assertEquals("SD-P0001", patient.getRegNo());
        }
    }

    @Nested
    @DisplayName("Dentist entity")
    class DentistEntity {

        @Test
        @DisplayName("builder sets fields")
        void builder() {
            Dentist dentist = Dentist.builder()
                    .id(1L).licenceNo("DR-0001").firstName("Jane").lastName("Smith")
                    .specialization("Orthodontics").contactNumber("0771234567")
                    .email("jane@example.com").status(DentistStatus.AVAILABLE)
                    .yearsOfExperience(5).biography("Experienced")
                    .build();

            assertEquals("DR-0001", dentist.getLicenceNo());
            assertEquals(DentistStatus.AVAILABLE, dentist.getStatus());
            assertEquals(5, dentist.getYearsOfExperience());
            assertEquals("Jane Smith", dentist.getFirstName() + " " + dentist.getLastName());
        }
    }

    @Nested
    @DisplayName("Treatment entity")
    class TreatmentEntity {

        @Test
        @DisplayName("builder sets fields")
        void builder() {
            Treatment treatment = Treatment.builder()
                    .id(1L).code("TRT-CLEAN").name("Cleaning")
                    .description("Deep clean").category("Preventive")
                    .cost(5000.0).durationMinutes(30).active(true)
                    .build();

            assertEquals("TRT-CLEAN", treatment.getCode());
            assertEquals(5000.0, treatment.getCost());
            assertEquals(30, treatment.getDurationMinutes());
            assertTrue(treatment.isActive());
        }
    }

    @Nested
    @DisplayName("Appointment entity")
    class AppointmentEntity {

        @Test
        @DisplayName("builder sets fields")
        void builder() {
            Patient patient = new Patient();
            Dentist dentist = new Dentist();
            Treatment treatment = new Treatment();
            LocalDate date = LocalDate.of(2026, 1, 1);
            Appointment appointment = Appointment.builder()
                    .id(1L).appointmentNumber("AP-2026-0001")
                    .patient(patient).dentist(dentist).treatment(treatment)
                    .appointmentDate(date)
                    .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30))
                    .status(AppointmentStatus.SCHEDULED)
                    .notes("Some notes").completedNotes("Done")
                    .build();

            assertEquals("AP-2026-0001", appointment.getAppointmentNumber());
            assertEquals(date, appointment.getAppointmentDate());
            assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
            assertEquals("Some notes", appointment.getNotes());
            assertEquals(LocalTime.of(9, 30), appointment.getEndTime());
        }
    }

    @Nested
    @DisplayName("Bill entity")
    class BillEntity {

        @Test
        @DisplayName("builder sets financial fields")
        void builder() {
            Appointment appointment = new Appointment();
            Bill bill = Bill.builder()
                    .id(1L).billNumber("INV-0001").appointment(appointment)
                    .treatmentCost(5000.0).consultationFee(1500.0)
                    .discount(0.0).tax(650.0).totalAmount(7150.0)
                    .amountPaid(5000.0).dueAmount(2150.0)
                    .paymentStatus(PaymentStatus.PARTIAL)
                    .paymentMethod(PaymentMethod.CASH)
                    .billedAt(LocalDateTime.now())
                    .build();

            assertEquals("INV-0001", bill.getBillNumber());
            assertEquals(7150.0, bill.getTotalAmount());
            assertEquals(2150.0, bill.getDueAmount());
            assertEquals(PaymentStatus.PARTIAL, bill.getPaymentStatus());
            assertEquals(PaymentMethod.CASH, bill.getPaymentMethod());
        }
    }

    @Nested
    @DisplayName("Payment entity")
    class PaymentEntity {

        @Test
        @DisplayName("builder sets fields")
        void builder() {
            Bill bill = new Bill();
            Payment payment = Payment.builder()
                    .id(1L).bill(bill).amount(5000.0)
                    .paymentMethod(PaymentMethod.CARD)
                    .referenceNo("TXN-123").remarks("Online")
                    .paymentDate(LocalDateTime.now())
                    .build();

            assertEquals(5000.0, payment.getAmount());
            assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
            assertEquals("TXN-123", payment.getReferenceNo());
        }
    }

    @Nested
    @DisplayName("User entity")
    class UserEntity {

        @Test
        @DisplayName("builder sets fields")
        void builder() {
            User user = User.builder()
                    .id(1L).username("admin").password("encoded")
                    .email("admin@example.com").fullName("Admin")
                    .contactNumber("0771234567").role(Role.ADMIN)
                    .active(true).accountLocked(false).failedAttempts(0)
                    .build();

            assertEquals("admin", user.getUsername());
            assertEquals(Role.ADMIN, user.getRole());
            assertTrue(user.isActive());
            assertFalse(user.isAccountLocked());
            assertEquals(0, user.getFailedAttempts());
        }

        @Test
        @DisplayName("defaults are false/0")
        void defaults() {
            User user = new User();
            assertFalse(user.isActive());
            assertFalse(user.isAccountLocked());
            assertEquals(0, user.getFailedAttempts());
        }
    }

    @Nested
    @DisplayName("Notification entity")
    class NotificationEntity {

        @Test
        @DisplayName("builder sets fields")
        void builder() {
            LocalDateTime now = LocalDateTime.now();
            Notification notification = Notification.builder()
                    .id(1L).recipient("john@example.com")
                    .channel(NotificationChannel.EMAIL)
                    .subject("Subject").content("Content")
                    .status(NotificationStatus.SENT)
                    .sentAt(now).readAt(now)
                    .errorMessage(null)
                    .build();

            assertEquals("john@example.com", notification.getRecipient());
            assertEquals(NotificationChannel.EMAIL, notification.getChannel());
            assertEquals(NotificationStatus.SENT, notification.getStatus());
            assertEquals(now, notification.getSentAt());
        }
    }

    @Nested
    @DisplayName("NumberSequence entity")
    class NumberSequenceEntity {

        @Test
        @DisplayName("builder sets fields")
        void builder() {
            NumberSequence sequence = NumberSequence.builder()
                    .id(1L).sequenceKey("PATIENT").currentValue(42).prefix("SD-P").version(1)
                    .build();

            assertEquals("PATIENT", sequence.getSequenceKey());
            assertEquals(42L, sequence.getCurrentValue());
            assertEquals("SD-P", sequence.getPrefix());
        }

        @Test
        @DisplayName("mutators update values")
        void mutators() {
            NumberSequence sequence = new NumberSequence();
            sequence.setCurrentValue(100);
            assertEquals(100L, sequence.getCurrentValue());
        }
    }

    @Nested
    @DisplayName("AuditLog entity")
    class AuditLogEntity {

        @Test
        @DisplayName("builder sets fields")
        void builder() {
            LocalDateTime ts = LocalDateTime.now();
            AuditLog log = AuditLog.builder()
                    .id(1L).username("admin").action("CREATE")
                    .entityType("Patient").entityId(1L)
                    .details("Details").ipAddress("127.0.0.1").timestamp(ts)
                    .build();

            assertEquals("admin", log.getUsername());
            assertEquals("CREATE", log.getAction());
            assertEquals("Patient", log.getEntityType());
            assertEquals(ts, log.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Auditable base class")
    class AuditableTest {

        @Test
        @DisplayName("setters and getters work")
        void setAndGet() {
            Patient patient = new Patient();
            LocalDateTime now = LocalDateTime.now();
            patient.setCreatedBy("admin");
            patient.setCreatedDate(now);
            patient.setLastModifiedBy("receptionist");
            patient.setLastModifiedDate(now);

            assertEquals("admin", patient.getCreatedBy());
            assertEquals(now, patient.getCreatedDate());
            assertEquals("receptionist", patient.getLastModifiedBy());
            assertEquals(now, patient.getLastModifiedDate());
        }
    }
}
