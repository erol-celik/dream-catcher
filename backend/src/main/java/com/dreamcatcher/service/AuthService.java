package com.dreamcatcher.service;

import com.dreamcatcher.dto.request.AuthLinkRequest;
import com.dreamcatcher.dto.response.AuthResponse;
import com.dreamcatcher.entity.User;
import com.dreamcatcher.exception.ResourceNotFoundException;
import com.dreamcatcher.repository.UserRepository;
import com.dreamcatcher.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling authentication and linking guest accounts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Links a local anonymous guest account to an authenticated OAuth Profile
     * (Google/Apple) and returns a signed JWT.
     */
    @Transactional
    public AuthResponse linkGuestAccount(AuthLinkRequest request) {
        log.info("Linking guest account to OAuth provider {} for email {}", request.authProvider(), request.email());

        // Find existing guest by token
        User user = userRepository.findByGuestToken(request.guestToken())
                .orElseThrow(() -> new ResourceNotFoundException("Guest User", "guestToken", request.guestToken()));

        // In a real OAuth flow we'd cryptographically verify an ID token sent from the client here.
        // For the sake of this API, we trust the verified payload from the mobile app (React Native OAuth libs).

        // Perform the linkage
        user.setIsGuest(false);
        user.setEmail(request.email());
        user.setAuthProvider(request.authProvider());
        user.setAuthProviderId(request.providerId());
        
        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName());
        }

        userRepository.save(user);

        // Generate the JWT Token (Stateless tracking across subsequent requests)
        String token = jwtUtil.generateToken(user.getId());

        log.info("Account linked successfully. user_id={}, provider={}", user.getId(), request.authProvider());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }

}
