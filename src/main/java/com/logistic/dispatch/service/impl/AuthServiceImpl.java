package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.LoginRequest;
import com.logistic.dispatch.dto.LoginResponse;
import com.logistic.dispatch.entitiy.UserInfo;
import com.logistic.dispatch.exception.InvalidCredentialsException;
import com.logistic.dispatch.repository.UserInfoRepository;
import com.logistic.dispatch.security.JwtUtil;
import com.logistic.dispatch.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserInfoRepository userRepo;
    private final JwtUtil jwtUtil;
    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserInfoRepository userRepo,
                           JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {


            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            LOG.error("Authentication failed for user: {}", request.getUsername(), e);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UserInfo user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name());
        LOG.info("Authentication successful for user: {}", request.getUsername());
        return new LoginResponse(
                user.getUserId(),
                user.getName(),
                user.getUsername(),
                user.getRole().name(),
                token
        );
    }
}