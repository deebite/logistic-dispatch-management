package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.CreateUserRequestDto;
import com.logistic.dispatch.dto.UserResponseDto;
import com.logistic.dispatch.utility.ProfileStatus;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDto createUser(CreateUserRequestDto requestDTO);

    UserResponseDto getUserById(UUID id);

    List<UserResponseDto> getAllUsers();

    UserResponseDto updateUser(UUID id, CreateUserRequestDto dto);

    UserResponseDto patchUser(UUID id, CreateUserRequestDto dto);

    UserResponseDto changeUserStatus(UUID id, ProfileStatus status);

    void deleteUser(UUID id);
}
