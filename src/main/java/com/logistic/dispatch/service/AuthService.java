package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.LoginRequest;
import com.logistic.dispatch.dto.LoginResponse;
import com.logistic.dispatch.dto.LogoutRequestDto;
import com.logistic.dispatch.dto.LogoutResponseDto;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    LogoutResponseDto logout(LogoutRequestDto request);
}
