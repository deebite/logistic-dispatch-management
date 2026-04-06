package com.logistic.dispatch.mapper;

import com.logistic.dispatch.dto.CreateUserRequestDto;
import com.logistic.dispatch.dto.UserResponseDto;
import com.logistic.dispatch.entitiy.UserInfo;
import com.logistic.dispatch.utility.ProfileStatus;
import com.logistic.dispatch.utility.UserRole;

public class UserMapper {

    public static UserInfo toEntity(CreateUserRequestDto dto) {

        UserInfo user = new UserInfo();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
//        user.setRole(UserRole.valueOf(dto.getRole().toUpperCase()));
        user.setProfileStatus(ProfileStatus.ACTIVE);

        return user;
    }

    public static UserResponseDto toResponse(UserInfo user) {

        return new UserResponseDto(
                user.getUserId(),
                user.getName(),
                user.getUsername(),
                user.getRole().name(),
                user.getProfileStatus().name()
        );
    }
}
