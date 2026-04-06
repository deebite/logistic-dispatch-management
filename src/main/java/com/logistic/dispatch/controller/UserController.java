package com.logistic.dispatch.controller;

import com.logistic.dispatch.dto.CreateUserRequestDto;
import com.logistic.dispatch.dto.UpdateUserStatusDto;
import com.logistic.dispatch.dto.UserResponseDto;
import com.logistic.dispatch.service.UserService;
import com.logistic.dispatch.utility.ProfileStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee/users")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto requestDTO) {
        UserResponseDto response = userService.createUser(requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID id, @Valid @RequestBody CreateUserRequestDto dto) {

        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> patchUser(@PathVariable UUID id, @RequestBody CreateUserRequestDto dto) {
        return ResponseEntity.ok(userService.patchUser(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponseDto> changeUserStatus(@PathVariable UUID id, @RequestParam String status) {
        ProfileStatus profileStatus = ProfileStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(userService.changeUserStatus(id, profileStatus));
    }

    @GetMapping("/by/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}