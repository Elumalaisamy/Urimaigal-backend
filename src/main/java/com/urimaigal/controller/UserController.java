package com.urimaigal.controller;

import com.urimaigal.dto.ApiResponse;
import com.urimaigal.model.User;
import com.urimaigal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GET   /api/users/me          — get current user profile
 * PATCH /api/users/me          — update profile fields
 * GET   /api/users/{id}        — get user by id (admin)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getProfile(
            @AuthenticationPrincipal String userId) {
        User user = userService.getUserById(userId);
        // Never expose password hash
        user.setPassword(null);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> updates) {
        User updated = userService.updateProfile(
                userId,
                updates.get("name"),
                updates.get("phone"),
                updates.get("avatar"));
        updated.setPassword(null);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        user.setPassword(null);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }
}
