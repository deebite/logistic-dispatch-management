package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.LoginRequest;
import com.logistic.dispatch.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);
}
