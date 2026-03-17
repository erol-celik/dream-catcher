package com.dreamcatcher.controller;

import com.dreamcatcher.dto.request.GuestRegistrationRequest;
import com.dreamcatcher.dto.response.GuestRegistrationResponse;
import com.dreamcatcher.dto.response.UserProfileResponse;
import com.dreamcatcher.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management operations.
 * Handles guest registration and profile retrieval.
 * OAuth2 authentication endpoints will be added in the Auth module.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Registers a new guest user.
     * POST /api/v1/users/guest
     */
    @PostMapping("/guest")
    public ResponseEntity<GuestRegistrationResponse> registerGuest(
            @Valid @RequestBody GuestRegistrationRequest request) {
        GuestRegistrationResponse response = userService.registerGuest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the user's profile with summary statistics.
     * GET /api/v1/users/profile
     *
     * Note: userId is extracted securely from the JWT token.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

}
