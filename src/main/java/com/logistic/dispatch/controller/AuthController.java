package com.logistic.dispatch.controller;

import com.logistic.dispatch.dto.LoginRequest;
import com.logistic.dispatch.dto.LoginResponse;
import com.logistic.dispatch.dto.LogoutRequestDto;
import com.logistic.dispatch.dto.LogoutResponseDto;
import com.logistic.dispatch.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDto> logout(@RequestBody LogoutRequestDto request) {
        return ResponseEntity.ok(authService.logout(request));
    }
}
