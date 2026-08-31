package com.sunrise.dental.mapper;

import com.sunrise.dental.dto.response.BillResponse;
import com.sunrise.dental.entity.Bill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillMapper {

    @Mapping(target = "appointmentId", source = "bill.appointment.id")
    @Mapping(target = "appointmentNumber", source = "bill.appointment.appointmentNumber")
    @Mapping(target = "patientId", source = "bill.appointment.patient.id")
    @Mapping(target = "patientName", expression = "java(bill.getAppointment().getPatient().getFullName())")
    @Mapping(target = "dentistId", source = "bill.appointment.dentist.id")
    @Mapping(target = "dentistName", expression = "java(bill.getAppointment().getDentist().getFirstName() + \" \" + bill.getAppointment().getDentist().getLastName())")
    @Mapping(target = "treatmentName", source = "bill.appointment.treatment.name")
    @Mapping(target = "treatmentCode", source = "bill.appointment.treatment.code")
    @Mapping(target = "appointmentDate", expression = "java(bill.getAppointment().getAppointmentDate().atTime(bill.getAppointment().getStartTime()))")
    BillResponse toResponse(Bill bill);
}
