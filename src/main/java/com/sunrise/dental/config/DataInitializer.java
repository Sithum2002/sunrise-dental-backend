package com.sunrise.dental.config;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.entity.Dentist;
import com.sunrise.dental.entity.NumberSequence;
import com.sunrise.dental.entity.Patient;
import com.sunrise.dental.entity.Treatment;
import com.sunrise.dental.entity.User;
import com.sunrise.dental.enums.BloodGroup;
import com.sunrise.dental.enums.DentistStatus;
import com.sunrise.dental.enums.Gender;
import com.sunrise.dental.enums.Role;
import com.sunrise.dental.repository.DentistRepository;
import com.sunrise.dental.repository.NumberSequenceRepository;
import com.sunrise.dental.repository.PatientRepository;
import com.sunrise.dental.repository.TreatmentRepository;
import com.sunrise.dental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds reference/master data on first startup (dev/demo profiles).
 * Idempotent - safe to run multiple times.
 */
@Slf4j
@Component
@Profile({"dev", "default"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;
    private final PatientRepository patientRepository;
    private final NumberSequenceRepository numberSequenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedDentists();
        seedTreatments();
        seedSequences();
        log.info("Seed data initialisation complete.");
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        List<User> users = List.of(
                User.builder().username("admin").password(passwordEncoder.encode("Admin@123"))
                        .email("admin@sunrisedental.lk").fullName("System Administrator")
                        .contactNumber("+94771234561").role(Role.ADMIN).active(true).build(),
                User.builder().username("receptionist").password(passwordEncoder.encode("Rec@12345"))
                        .email("receptionist@sunrisedental.lk").fullName("Nimali Perera")
                        .contactNumber("+94771234562").role(Role.RECEPTIONIST).active(true).build(),
                User.builder().username("dr.perera").password(passwordEncoder.encode("Doctor@123"))
                        .email("dr.perera@sunrisedental.lk").fullName("Dr. Kasun Perera")
                        .contactNumber("+94771234563").role(Role.DOCTOR).active(true).build()
        );
        userRepository.saveAll(users);
        auditService.log("SEED", "User", null, "Seeded default users (admin, receptionist, doctor)");
    }

    private void seedDentists() {
        if (dentistRepository.count() > 0) {
            return;
        }
        List<Dentist> dentists = List.of(
                Dentist.builder().licenceNo("DR-1001").firstName("Kasun").lastName("Perera")
                        .specialization("General Dentistry").contactNumber("+94771234570")
                        .email("dr.perera@sunrisedental.lk").status(DentistStatus.AVAILABLE)
                        .yearsOfExperience(12).joiningDate(LocalDate.of(2015, 3, 1))
                        .biography("Lead general dentist with over a decade of experience.").build(),
                Dentist.builder().licenceNo("DR-1002").firstName("Sachini").lastName("Fernando")
                        .specialization("Orthodontics").contactNumber("+94771234571")
                        .email("dr.fernando@sunrisedental.lk").status(DentistStatus.AVAILABLE)
                        .yearsOfExperience(8).joiningDate(LocalDate.of(2018, 6, 15))
                        .biography("Specialist orthodontist focusing on braces and aligners.").build(),
                Dentist.builder().licenceNo("DR-1003").firstName("Ravindu").lastName("Silva")
                        .specialization("Endodontics (Root Canal)").contactNumber("+94771234572")
                        .email("dr.silva@sunrisedental.lk").status(DentistStatus.AVAILABLE)
                        .yearsOfExperience(6).joiningDate(LocalDate.of(2020, 1, 10))
                        .biography("Endodontic specialist for root canal treatments.").build(),
                Dentist.builder().licenceNo("DR-1004").firstName("Tharindi").lastName("Jayasinghe")
                        .specialization("Cosmetic Dentistry").contactNumber("+94771234573")
                        .email("dr.jayasinghe@sunrisedental.lk").status(DentistStatus.ON_LEAVE)
                        .yearsOfExperience(5).joiningDate(LocalDate.of(2021, 9, 1))
                        .biography("Cosmetic dentistry including whitening and veneers.").build()
        );
        dentistRepository.saveAll(dentists);
        auditService.log("SEED", "Dentist", null, "Seeded 4 dentists");
    }

    private void seedTreatments() {
        if (treatmentRepository.count() > 0) {
            return;
        }
        List<Treatment> treatments = List.of(
                Treatment.builder().code("TRT-CLN").name("Dental Cleaning / Scaling")
                        .description("Professional scaling and polishing.").category("Preventive")
                        .cost(8000.00).durationMinutes(30).active(true).build(),
                Treatment.builder().code("TRT-FIL").name("Tooth Filling (Composite)")
                        .description("Composite resin filling for cavities.").category("Restorative")
                        .cost(12000.00).durationMinutes(45).active(true).build(),
                Treatment.builder().code("TRT-RCT").name("Root Canal Treatment")
                        .description("Endodontic therapy.").category("Endodontics")
                        .cost(45000.00).durationMinutes(90).active(true).build(),
                Treatment.builder().code("TRT-EXT").name("Tooth Extraction")
                        .description("Simple and surgical extractions.").category("Oral Surgery")
                        .cost(15000.00).durationMinutes(45).active(true).build(),
                Treatment.builder().code("TRT-BRC").name("Braces Fitting")
                        .description("Orthodontic brace fitting consultation + fitting.").category("Orthodontics")
                        .cost(185000.00).durationMinutes(60).active(true).build(),
                Treatment.builder().code("TRT-CRN").name("Crown / Bridge")
                        .description("Porcelain crowns and bridges.").category("Prosthodontics")
                        .cost(65000.00).durationMinutes(60).active(true).build(),
                Treatment.builder().code("TRT-WHT").name("Teeth Whitening")
                        .description("In-clinic professional whitening.").category("Cosmetic")
                        .cost(25000.00).durationMinutes(45).active(true).build(),
                Treatment.builder().code("TRT-IMP").name("Dental Implant")
                        .description("Single tooth implant placement.").category("Implantology")
                        .cost(180000.00).durationMinutes(90).active(true).build(),
                Treatment.builder().code("TRT-XRY").name("Digital X-Ray")
                        .description("Diagnostic digital radiograph.").category("Diagnostic")
                        .cost(3500.00).durationMinutes(15).active(true).build()
        );
        treatmentRepository.saveAll(treatments);
        auditService.log("SEED", "Treatment", null, "Seeded treatment catalogue");
    }

    private void seedSequences() {
        if (numberSequenceRepository.count() > 0) {
            return;
        }
        numberSequenceRepository.saveAll(List.of(
                NumberSequence.builder().sequenceKey("PATIENT").currentValue(1).prefix("SD-P").build(),
                NumberSequence.builder().sequenceKey("APPOINTMENT").currentValue(1).prefix("AP").build(),
                NumberSequence.builder().sequenceKey("BILL").currentValue(1).prefix("INV").build(),
                NumberSequence.builder().sequenceKey("USER").currentValue(1).prefix("USR").build()
        ));
    }
}
