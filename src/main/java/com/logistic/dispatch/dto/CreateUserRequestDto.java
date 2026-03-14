package com.logistic.dispatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters long")
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters long")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Name must be between 8 and 20 characters long")
    private String password;

    @NotBlank(message = "Role is required")
    private String role;
}