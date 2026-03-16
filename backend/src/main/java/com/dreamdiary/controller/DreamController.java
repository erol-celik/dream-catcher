package com.dreamdiary.controller;

import com.dreamdiary.dto.request.CreateDreamRequest;
import com.dreamdiary.dto.response.DreamResponse;
import com.dreamdiary.service.DreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for dream entry operations.
 * Handles creating, listing, and retrieving individual dreams.
 * Dream text goes through heuristic validation before persistence.
 */
@RestController
@RequestMapping("/api/v1/dreams")
@RequiredArgsConstructor
public class DreamController {

    private final DreamService dreamService;

    /**
     * Creates a new dream entry with heuristic validation.
     * POST /api/v1/dreams?userId={userId}
     *
     * Returns 422 if the dream text fails validation (too short, gibberish).
     * Returns 409 if a dream with the same clientId already exists.
     */
    @PostMapping
    public ResponseEntity<DreamResponse> createDream(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateDreamRequest request) {
        DreamResponse response = dreamService.createDream(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all dreams for a user, ordered by dream date descending.
     * GET /api/v1/dreams
     */
    @GetMapping
    public ResponseEntity<List<DreamResponse>> getDreams(@AuthenticationPrincipal Long userId) {
        List<DreamResponse> dreams = dreamService.getDreamsByUser(userId);
        return ResponseEntity.ok(dreams);
    }

    /**
     * Retrieves a single dream by its server-side ID.
     * GET /api/v1/dreams/{dreamId}
     * Verifies dream ownership via the authenticated user's ID.
     */
    @GetMapping("/{dreamId}")
    public ResponseEntity<DreamResponse> getDream(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dreamId) {
        DreamResponse response = dreamService.getDreamById(userId, dreamId);
        return ResponseEntity.ok(response);
    }

}
