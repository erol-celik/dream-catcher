package com.dreamdiary.controller;

import com.dreamdiary.dto.request.AuthLinkRequest;
import com.dreamdiary.dto.response.AuthResponse;
import com.dreamdiary.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates via an OAuth provider, locating the existing anonymous user
     * using the local guest token, merging the account data, and returning a valid JWT.
     */
    @PostMapping("/link-guest")
    public ResponseEntity<AuthResponse> linkGuestAccount(@Valid @RequestBody AuthLinkRequest request) {
        AuthResponse response = authService.linkGuestAccount(request);
        return ResponseEntity.ok(response);
    }
    
}
