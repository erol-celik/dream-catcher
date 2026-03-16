package com.dreamdiary.controller;

import com.dreamdiary.dto.request.SyncRequest;
import com.dreamdiary.dto.response.SyncResponse;
import com.dreamdiary.service.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for offline-first bulk sync operations.
 * The mobile client calls this endpoint when it comes online
 * to push all locally stored dreams and activities.
 */
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    /**
     * Processes a bulk sync request from the mobile client.
     * POST /api/v1/sync
     *
     * Each item in the batch is processed independently.
     * The response reports per-item success/failure so the client
     * can update its local is_synced flags.
     */
    @PostMapping
    public ResponseEntity<SyncResponse> syncData(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SyncRequest request) {
        SyncResponse response = syncService.syncData(userId, request);
        return ResponseEntity.ok(response);
    }

}
