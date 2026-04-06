package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.CreateUserRequestDto;
import com.logistic.dispatch.dto.UserResponseDto;
import com.logistic.dispatch.entitiy.UserInfo;
import com.logistic.dispatch.exception.SelfDeactivationNotAllowedException;
import com.logistic.dispatch.exception.UserNotFoundException;
import com.logistic.dispatch.exception.UsernameAlreadyExistsException;
import com.logistic.dispatch.mapper.UserMapper;
import com.logistic.dispatch.repository.UserInfoRepository;
import com.logistic.dispatch.service.UserService;
import com.logistic.dispatch.utility.ProfileStatus;
import com.logistic.dispatch.utility.UserRole;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserInfoRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserInfoRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDto createUser(CreateUserRequestDto requestDTO) {

        // ✅ Get logged-in user role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInRole = authentication.getAuthorities().iterator().next().getAuthority();
        loggedInRole = loggedInRole.replace("ROLE_", "");

        UserRole loggedInUserRole = UserRole.valueOf(loggedInRole);
        UserRole requestedRole = UserRole.valueOf(requestDTO.getRole().toUpperCase());

        if (loggedInUserRole == UserRole.OPERATOR) {
            LOG.error("Operator is not allowed to create users");
            throw new UnsupportedOperationException("Operator is not allowed to create users!");
        }

        if (loggedInUserRole == UserRole.SUPERVISOR && requestedRole != UserRole.OPERATOR) {
            LOG.error("Supervisor can only create OPERATOR users!");
            throw new UnsupportedOperationException("Supervisor can only create OPERATOR users!");
        }

        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            LOG.error("Username already exists!");
            throw new UsernameAlreadyExistsException("Username already exists!");
        }

        UserInfo user = UserMapper.toEntity(requestDTO);
        user.setRole(requestedRole);
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setProfileStatus(ProfileStatus.ACTIVE);

        LOG.info("LoggedIn Role: {} Requested Role to create: {}", loggedInUserRole, requestedRole);

        LOG.info("User Created: {}", user);
        UserInfo savedUser = userRepository.save(user);
        return UserMapper.toResponse(savedUser);
    }

    @Override
    public UserResponseDto getUserById(UUID id) {

        UserInfo user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserMapper.toResponse(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
    }

    @Override
    public UserResponseDto updateUser(UUID id, CreateUserRequestDto dto) {

        UserInfo user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getUsername().equals(dto.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new UsernameAlreadyExistsException("Username already exists");
            }
            user.setUsername(dto.getUsername());
        }
        user.setName(dto.getName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.valueOf(dto.getRole().toUpperCase()));

        UserInfo updated = userRepository.save(user);

        return UserMapper.toResponse(updated);
    }

    @Override
    public UserResponseDto patchUser(UUID id, CreateUserRequestDto dto) {

        UserInfo user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (dto.getName() != null)
            user.setName(dto.getName());

        if (dto.getUsername() != null)
            user.setUsername(dto.getUsername());

        if (dto.getPassword() != null)
            user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getRole() != null)
            user.setRole(UserRole.valueOf(dto.getRole().toUpperCase()));

        UserInfo updated = userRepository.save(user);

        return UserMapper.toResponse(updated);
    }

    @Override
    public UserResponseDto changeUserStatus(UUID id, ProfileStatus status) {

        UserInfo user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 🔥 Prevent self-deactivation
        String loggedInUsername = Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName();

        if (user.getUsername().equals(loggedInUsername) && status == ProfileStatus.INACTIVE) {
            throw new SelfDeactivationNotAllowedException("You cannot deactivate yourself");
        }

        user.setProfileStatus(status);
        UserInfo updatedUser = userRepository.save(user);

        return UserMapper.toResponse(updatedUser);
    }


    @Override
    public void deleteUser(UUID id) {

        UserInfo user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public UserResponseDto getUserByUsername(String username) {
        UserInfo user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserMapper.toResponse(user);
    }
}
