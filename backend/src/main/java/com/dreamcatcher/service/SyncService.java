package com.dreamcatcher.service;

import com.dreamcatcher.dto.request.CreateDreamRequest;
import com.dreamcatcher.dto.request.DailyActivityRequest;
import com.dreamcatcher.dto.request.SyncRequest;
import com.dreamcatcher.dto.response.SyncResponse;
import com.dreamcatcher.dto.response.SyncResponse.SyncedItemResult;
import com.dreamcatcher.entity.DailyActivity;
import com.dreamcatcher.entity.Dream;
import com.dreamcatcher.entity.WeeklyReport;
import com.dreamcatcher.enums.ReportStatus;
import com.dreamcatcher.repository.DreamRepository;
import com.dreamcatcher.repository.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
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
    
    private final WeeklyReportRepository weeklyReportRepository;
    private final DreamRepository dreamRepository;
    private final WeeklyAnalysisService weeklyAnalysisService;

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
        
        // Lazy evaluate previous calendar week
        boolean newAnalysisTriggered = false;
        try {
            newAnalysisTriggered = evaluatePreviousWeek(userId);
        } catch (Exception e) {
            log.error("Failed to evaluate previous week for user={}: {}", userId, e.getMessage());
        }

        log.info("Sync completed for user={}: synced={}, failed={}", userId, totalSynced, totalFailed);

        return new SyncResponse(dreamResults, activityResults, totalSynced, totalFailed, newAnalysisTriggered);
    }

    /**
     * Synchronously checks if a report exists for the previous Monday-Sunday week.
     * If not, counts dreams and saves either an INSUFFICIENT_DATA or PENDING report.
     * Only calls the async AI service if PENDING is saved.
     */
    private boolean evaluatePreviousWeek(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate lastSunday = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        LocalDate lastMonday = lastSunday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        if (weeklyReportRepository.existsByUserIdAndWeekStart(userId, lastMonday)) {
            return false;
        }
        
        long validDreamsCount = dreamRepository.countValidDreamsByUserIdAndDateRange(userId, lastMonday, lastSunday);
        
        WeeklyReport report = new WeeklyReport();
        // Set user reference (only ID is needed for saving, we construct a detached entity)
        com.dreamcatcher.entity.User userRef = new com.dreamcatcher.entity.User();
        userRef.setId(userId);
        report.setUser(userRef);
        report.setWeekStart(lastMonday);
        report.setWeekEnd(lastSunday);
        report.setDreamCount((int) validDreamsCount);
        
        if (validDreamsCount < 3) {
            report.setStatus(ReportStatus.INSUFFICIENT_DATA);
            weeklyReportRepository.save(report);
            log.info("Created INSUFFICIENT_DATA report for user={}, weekStart={}, count={}", userId, lastMonday, validDreamsCount);
            return false;
        } else {
            report.setStatus(ReportStatus.PENDING);
            report = weeklyReportRepository.save(report);
            log.info("Created PENDING report for user={}, weekStart={}, count={}. Triggering analysis.", userId, lastMonday, validDreamsCount);
            weeklyAnalysisService.processReportAsync(report.getId(), lastMonday, lastSunday);
            return true;
        }
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
