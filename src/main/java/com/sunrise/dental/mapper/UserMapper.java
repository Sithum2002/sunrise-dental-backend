package com.sunrise.dental.mapper;

import com.sunrise.dental.dto.response.UserResponse;
import com.sunrise.dental.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
