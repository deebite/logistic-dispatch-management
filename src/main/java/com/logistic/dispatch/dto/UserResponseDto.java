package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponseDto {

    private UUID userId;

    private String name;

    private String username;

    private String role;

    private String profileStatus;
}
