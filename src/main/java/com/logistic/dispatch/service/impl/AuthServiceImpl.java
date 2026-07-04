package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.LoginRequest;
import com.logistic.dispatch.dto.LoginResponse;
import com.logistic.dispatch.dto.LogoutRequestDto;
import com.logistic.dispatch.dto.LogoutResponseDto;
import com.logistic.dispatch.entitiy.UserInfo;
import com.logistic.dispatch.exception.InvalidCredentialsException;
import com.logistic.dispatch.exception.UserNotFoundException;
import com.logistic.dispatch.repository.UserInfoRepository;
import com.logistic.dispatch.security.JwtUtil;
import com.logistic.dispatch.service.AuthService;
import com.logistic.dispatch.service.BatchService;
import com.logistic.dispatch.utility.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserInfoRepository userRepo;
    private final JwtUtil jwtUtil;
    private final BatchService batchService;
    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserInfoRepository userRepo, JwtUtil jwtUtil, BatchService batchService) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.batchService = batchService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            LOG.error("Authentication failed for user: {} and exception: {}", request.getUsername(), e.getMessage());
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

    @Override
    @Transactional
    public LogoutResponseDto logout(LogoutRequestDto request) {
        LOG.info("Logging out user: {}", SecurityUtils.getCurrentUsername());
        String username = SecurityUtils.getCurrentUsername();

        UserInfo user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        int releasedBatchCount = 0;

        // Release batches only if requested
        if (Boolean.TRUE.equals(request.getReleaseBatches())) {
            releasedBatchCount = batchService.releaseAllActiveBatches(user.getUserId());
            LOG.info("User {} logged out and released {} active batches.", username, releasedBatchCount);

        } else {
            LOG.info("User {} logged out and kept active batches for resume.", username);
        }

        return LogoutResponseDto.builder()
                .message("Logout successful.")
                .releasedBatchCount(releasedBatchCount)
                .build();
    }
}