package com.sunrise.dental.mapper;

import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "patientId", source = "appointment.patient.id")
    @Mapping(target = "patientName", expression = "java(appointment.getPatient().getFullName())")
    @Mapping(target = "patientContact", source = "appointment.patient.contactNumber")
    @Mapping(target = "dentistId", source = "appointment.dentist.id")
    @Mapping(target = "dentistName", expression = "java(appointment.getDentist().getFirstName() + \" \" + appointment.getDentist().getLastName())")
    @Mapping(target = "treatmentId", source = "appointment.treatment.id")
    @Mapping(target = "treatmentName", source = "appointment.treatment.name")
    @Mapping(target = "treatmentCode", source = "appointment.treatment.code")
    AppointmentResponse toResponse(Appointment appointment);
}
