package com.dreamdiary.service;

import com.dreamdiary.dto.request.CreateDreamRequest;
import com.dreamdiary.dto.request.DailyActivityRequest;
import com.dreamdiary.dto.request.SyncRequest;
import com.dreamdiary.dto.response.SyncResponse;
import com.dreamdiary.dto.response.SyncResponse.SyncedItemResult;
import com.dreamdiary.entity.DailyActivity;
import com.dreamdiary.entity.Dream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service handling bulk sync operations from the mobile client.
 * Processes offline-stored dreams and activities.
 *
 * IMPORTANT: This orchestrator is intentionally NOT @Transactional.
 * Each item (dream/activity) is processed in its own transaction via
 * the downstream service methods. This ensures that a single item
 * failure does not roll back the entire batch.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final DreamService dreamService;
    private final DailyActivityService dailyActivityService;
    private final StreakService streakService;

    /**
     * Processes a bulk sync request.
     * Dreams are synced first (since activities may reference them),
     * then daily activities are synced.
     *
     * NOT @Transactional — each item runs in its own transaction
     * to isolate failures.
     */
    public SyncResponse syncData(Long userId, SyncRequest request) {
        log.info("Starting sync for user={}, dreams={}, activities={}",
                userId, request.dreams().size(), request.activities().size());

        List<SyncedItemResult> dreamResults = syncDreams(userId, request.dreams());
        List<SyncedItemResult> activityResults = syncActivities(userId, request.activities());

        int totalSynced = (int) (dreamResults.stream().filter(SyncedItemResult::success).count()
                + activityResults.stream().filter(SyncedItemResult::success).count());
        int totalFailed = (dreamResults.size() + activityResults.size()) - totalSynced;

        // Recalculate streak after full sync
        try {
            streakService.recordActivity(userId);
        } catch (Exception e) {
            log.warn("Failed to update streak after sync for user={}: {}", userId, e.getMessage());
        }

        log.info("Sync completed for user={}: synced={}, failed={}", userId, totalSynced, totalFailed);

        return new SyncResponse(dreamResults, activityResults, totalSynced, totalFailed);
    }

    private List<SyncedItemResult> syncDreams(Long userId, List<CreateDreamRequest> dreams) {
        List<SyncedItemResult> results = new ArrayList<>();

        for (CreateDreamRequest dreamReq : dreams) {
            try {
                Dream dream = dreamService.createDreamForSync(userId, dreamReq);
                results.add(SyncedItemResult.success(dreamReq.clientId(), dream.getId()));
            } catch (Exception e) {
                log.warn("Failed to sync dream clientId={}: {}", dreamReq.clientId(), e.getMessage());
                results.add(SyncedItemResult.failure(dreamReq.clientId(), e.getMessage()));
            }
        }

        return results;
    }

    private List<SyncedItemResult> syncActivities(Long userId, List<DailyActivityRequest> activities) {
        List<SyncedItemResult> results = new ArrayList<>();

        for (DailyActivityRequest activityReq : activities) {
            try {
                DailyActivity activity = dailyActivityService.logActivityForSync(userId, activityReq);
                results.add(SyncedItemResult.success(activityReq.clientId(), activity.getId()));
            } catch (Exception e) {
                log.warn("Failed to sync activity clientId={}: {}", activityReq.clientId(), e.getMessage());
                results.add(SyncedItemResult.failure(activityReq.clientId(), e.getMessage()));
            }
        }

        return results;
    }

}
