package com.dreamcatcher.controller;

import com.dreamcatcher.dto.response.StreakResponse;
import com.dreamcatcher.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for streak information.
 */
@RestController
@RequestMapping("/api/v1/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;

    /**
     * Retrieves the current streak information for the authenticated user.
     * GET /api/v1/streaks
     */
    @GetMapping
    public ResponseEntity<StreakResponse> getStreak(@AuthenticationPrincipal Long userId) {
        StreakResponse response = streakService.getStreak(userId);
        return ResponseEntity.ok(response);
    }

}
