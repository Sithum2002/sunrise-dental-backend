package com.sunrise.dental.mapper;

import com.sunrise.dental.dto.request.DentistRequest;
import com.sunrise.dental.dto.response.DentistResponse;
import com.sunrise.dental.entity.Dentist;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DentistMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "licenceNo", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Dentist toEntity(DentistRequest request);

    @Mapping(target = "fullName", expression = "java(dentist.getFirstName() + \" \" + dentist.getLastName())")
    DentistResponse toResponse(Dentist dentist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "licenceNo", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntity(@MappingTarget Dentist dentist, DentistRequest request);
}
