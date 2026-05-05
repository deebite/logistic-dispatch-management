package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LoginResponse {

    private UUID userId;
    private String name;
    private String username;
    private String role;
    private String token;

    public LoginResponse(UUID userId, String name, String username, String role, String token) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.role = role;
        this.token = token;
    }

}