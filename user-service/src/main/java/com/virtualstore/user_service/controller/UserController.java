package com.virtualstore.user_service.controller;

import com.virtualstore.user_service.dto.request.UserRequest;
import com.virtualstore.user_service.dto.response.UserResponse;
import com.virtualstore.user_service.entity.Role;
import com.virtualstore.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    // ── CREATE ──────────────────────────────────────────────────────────────
    // Only ADMIN can create users directly; normal users register via
    // /auth/register

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(requestDTO));
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    // ADMIN can get any user; a USER can only get their own profile
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ADMIN can look up any email; a USER can only look up their own
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // ADMIN only
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ADMIN only
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // ADMIN only
    @GetMapping("/status/{id}")
    public ResponseEntity<UserResponse> getUsersByStatus(@PathVariable String id) {
        return ResponseEntity.ok(userService.enableUser(id));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    // ADMIN can update anyone; a USER can only update their own profile
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserRequest updated) {
        return ResponseEntity.ok(userService.updateUser(id, updated));
    }


    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    // ADMIN or self
    @PatchMapping("/{id}/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(@PathVariable String id) {
        return ResponseEntity.ok(userService.verifyEmail(id));
    }

    // ADMIN or self
    @PatchMapping("/{id}/verify-phone")
    public ResponseEntity<UserResponse> verifyPhone(@PathVariable String id) {
        return ResponseEntity.ok(userService.verifyPhone(id));
    }

    // ADMIN or self — called after login
    @PatchMapping("/{id}/last-login")
    public ResponseEntity<Void> updateLastLogin(@PathVariable String id) {
        userService.updateLastLogin(id);
        return ResponseEntity.noContent().build();
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    // ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}