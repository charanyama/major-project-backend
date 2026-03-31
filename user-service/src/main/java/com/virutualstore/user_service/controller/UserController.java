package com.virutualstore.user_service.controller;

import com.virutualstore.user_service.dto.UserRequestDTO;
import com.virutualstore.user_service.dto.UserResponseDTO;
import com.virutualstore.user_service.dto.UserUpdateDTO;
import com.virutualstore.user_service.entity.UserRole;
import com.virutualstore.user_service.entity.UserStatus;
import com.virutualstore.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── CREATE ──────────────────────────────────────────────────────────────
    // Only ADMIN can create users directly; normal users register via
    // /auth/register

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(requestDTO));
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    // ADMIN can get any user; a USER can only get their own profile
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // ADMIN can look up any email; a USER can only look up their own
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // ADMIN only
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ADMIN only
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // ADMIN only
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByStatus(@PathVariable UserStatus status) {
        return ResponseEntity.ok(userService.getUsersByStatus(status));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    // ADMIN can update anyone; a USER can only update their own profile
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        return ResponseEntity.ok(userService.updateUser(userId, updateDTO));
    }

    // ADMIN only — account lifecycle management
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<UserResponseDTO> activateUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.activateUser(userId));
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<UserResponseDTO> deactivateUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.deactivateUser(userId));
    }

    // ADMIN or self
    @PatchMapping("/{userId}/verify-email")
    public ResponseEntity<UserResponseDTO> verifyEmail(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.verifyEmail(userId));
    }

    // ADMIN or self
    @PatchMapping("/{userId}/verify-phone")
    public ResponseEntity<UserResponseDTO> verifyPhone(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.verifyPhone(userId));
    }

    // ADMIN or self — called after login
    @PatchMapping("/{userId}/last-login")
    public ResponseEntity<Void> updateLastLogin(@PathVariable UUID userId) {
        userService.updateLastLogin(userId);
        return ResponseEntity.noContent().build();
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    // ADMIN only
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}