package com.sunrise.dental.service.impl;

import com.sunrise.dental.dto.response.HelpTopicResponse;
import com.sunrise.dental.service.HelpService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Static in-memory help content rendered from a list of topics.
 * (In a fuller implementation this would live in the database.)
 */
@Service
public class HelpServiceImpl implements HelpService {

    private static final List<HelpTopicResponse> TOPICS = List.of(
            new HelpTopicResponse(1L, "How to log in to the system", "Getting Started",
                    "1. Open the clinic system URL.\n2. Enter your username and password.\n"
                            + "3. Press the Login button.\n4. If you forget your password, contact the system administrator "
                            + "to have it reset.\nTIP: Never share your password with anyone.", 1),
            new HelpTopicResponse(2L, "Register a new patient", "Patient Management",
                    "1. Go to Patients > New Patient.\n2. Fill in all required fields (name, address, contact, email, DOB, gender).\n"
                            + "3. Press Save. The system automatically assigns a registration number (SD-P####).\n"
                            + "4. The patient now appears in the patient list and can be booked for appointments.", 2),
            new HelpTopicResponse(3L, "Register a new appointment", "Appointments",
                    "1. Go to Appointments > New Appointment.\n2. Select the patient (search by name/reg no).\n"
                            + "3. Select the dentist and the treatment.\n4. Choose a date and time (08:00 - 17:30, weekdays only).\n"
                            + "5. Press Save. An appointment number (AP-YYYY-####) is generated and the patient is notified by email/SMS.", 3),
            new HelpTopicResponse(4L, "Search for an appointment", "Appointments",
                    "1. Go to Appointments.\n2. Enter the appointment number in the search box and press Search.\n"
                            + "3. Full patient and appointment details are displayed, including dentist, treatment, date and time.", 4),
            new HelpTopicResponse(5L, "Reschedule or cancel an appointment", "Appointments",
                    "1. Open the appointment (see 'Search for an appointment').\n2. Use the Reschedule button to pick a new date/time.\n"
                            + "3. Use the Cancel button if the visit will not happen - the patient is notified automatically.", 5),
            new HelpTopicResponse(6L, "Complete an appointment and create a bill", "Billing",
                    "1. When the treatment is done, open the appointment and press Complete.\n"
                            + "2. Go to Billing > New Bill, select the completed appointment and press Generate.\n"
                            + "3. The bill is calculated automatically: treatment cost + consultation fee (LKR 1,500) - discount + 10% tax.", 6),
            new HelpTopicResponse(7L, "Record a payment", "Billing",
                    "1. Open the bill.\n2. Enter the amount paid and the payment method (Cash/Card/Transfer/Insurance).\n"
                            + "3. Press Record Payment. The bill status updates to Paid or Partially Paid automatically.", 7),
            new HelpTopicResponse(8L, "Print a bill / receipt", "Reports",
                    "1. Go to Billing and open the bill.\n2. Press 'Print / PDF'.\n3. The system generates a PDF receipt you can print or save.", 8),
            new HelpTopicResponse(9L, "Generate management reports", "Reports",
                    "1. Go to Reports.\n2. Choose a report type (Appointments, Revenue, Dentist Performance, Treatment Popularity...).\n"
                            + "3. Set the date range if required and press Generate.\n4. The PDF report opens for viewing/downloading.", 9),
            new HelpTopicResponse(10L, "Manage treatments and dentists", "Administration",
                    "1. Admin users can manage the treatment catalogue and dentist profiles under Administration.\n"
                            + "2. Use the toggle buttons to activate/deactivate entries. Deactivated entries cannot be booked.", 10),
            new HelpTopicResponse(11L, "View notifications", "Notifications",
                    "1. Open Notifications from the main menu.\n2. All system notifications (email/SMS/in-app) are listed.\n"
                            + "3. Use Mark Read to acknowledge a notification.", 11),
            new HelpTopicResponse(12L, "Understand access roles", "Security",
                    "The system has three roles:\n- ADMIN: full control including users and reports.\n"
                            + "- RECEPTIONIST: manages patients, appointments and billing.\n"
                            + "- DOCTOR: views and manages their appointments.", 12)
    );

    @Override
    public List<HelpTopicResponse> getAllTopics() {
        return TOPICS;
    }

    @Override
    public HelpTopicResponse getByCategory(String category) {
        return TOPICS.stream()
                .filter(t -> t.category().equalsIgnoreCase(category))
                .findFirst()
                .orElse(TOPICS.get(0));
    }

    @Override
    public List<HelpTopicResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return TOPICS;
        }
        String q = query.toLowerCase(Locale.ROOT);
        return TOPICS.stream()
                .filter(t -> t.title().toLowerCase(Locale.ROOT).contains(q)
                        || t.category().toLowerCase(Locale.ROOT).contains(q)
                        || t.content().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }
}
