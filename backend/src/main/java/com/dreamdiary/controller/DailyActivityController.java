package com.dreamdiary.controller;

import com.dreamdiary.dto.request.DailyActivityRequest;
import com.dreamdiary.dto.response.DailyActivityResponse;
import com.dreamdiary.service.DailyActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for daily activity (habit loop) operations.
 * Supports logging DREAM, NO_DREAM, and GOAL activities.
 */
@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class DailyActivityController {

    private final DailyActivityService activityService;

    /**
     * Logs a daily activity entry.
     * POST /api/v1/activities?userId={userId}
     */
    @PostMapping
    public ResponseEntity<DailyActivityResponse> logActivity(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DailyActivityRequest request) {
        DailyActivityResponse response = activityService.logActivity(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists activities for a user within a date range.
     * GET /api/v1/activities?from={date}&to={date}
     */
    @GetMapping
    public ResponseEntity<List<DailyActivityResponse>> getActivities(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        // Default to last 30 days if no range specified
        LocalDate effectiveTo = (to != null) ? to : LocalDate.now();
        LocalDate effectiveFrom = (from != null) ? from : effectiveTo.minusDays(30);

        List<DailyActivityResponse> activities =
                activityService.getActivities(userId, effectiveFrom, effectiveTo);
        return ResponseEntity.ok(activities);
    }

}
