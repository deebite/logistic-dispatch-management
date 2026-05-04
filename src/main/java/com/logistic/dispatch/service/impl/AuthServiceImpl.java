package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.LoginRequest;
import com.logistic.dispatch.dto.LoginResponse;
import com.logistic.dispatch.entitiy.UserInfo;
import com.logistic.dispatch.repository.UserInfoRepository;
import com.logistic.dispatch.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserInfoRepository userInfoRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserInfoRepository userInfoRepository) {
        this.authenticationManager = authenticationManager;
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 🔥 Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        if (authentication.isAuthenticated()) {
            UserInfo user = userInfoRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return new LoginResponse(
                    user.getUsername(),
                    user.getRole().name()
            );
        }

        throw new RuntimeException("Invalid credentials");
    }
}
