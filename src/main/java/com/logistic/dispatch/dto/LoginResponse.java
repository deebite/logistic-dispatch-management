package com.logistic.dispatch.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private UUID userId;
    private String name;
    private String username;
    private String role;
    private String token;
}