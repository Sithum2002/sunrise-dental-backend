package com.sunrise.dental.mapper;

import com.sunrise.dental.dto.response.NotificationResponse;
import com.sunrise.dental.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
