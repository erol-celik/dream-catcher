package com.dreamdiary.service;

import com.dreamdiary.dto.request.DailyActivityRequest;
import com.dreamdiary.dto.response.DailyActivityResponse;
import com.dreamdiary.entity.DailyActivity;
import com.dreamdiary.entity.Dream;
import com.dreamdiary.entity.User;
import com.dreamdiary.enums.ActivityType;
import com.dreamdiary.exception.ResourceNotFoundException;
import com.dreamdiary.repository.DailyActivityRepository;
import com.dreamdiary.repository.DreamRepository;
import com.dreamdiary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service managing the daily habit loop.
 * Ensures one activity per day and handles the "I didn't dream" flow
 * with the dummy goal question to keep the streak alive.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyActivityService {

    private final DailyActivityRepository activityRepository;
    private final DreamRepository dreamRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;
    private final UserService userService;

    /**
     * Logs a daily activity. Only one activity per user per day.
     * If an activity already exists for today, it is updated (upsert behavior).
     */
    @Transactional
    public DailyActivityResponse logActivity(Long userId, DailyActivityRequest request) {
        log.info("Logging activity for user={}, type={}, date={}",
                userId, request.activityType(), request.activityDate());

        User user = userService.findUserById(userId);

        // Check for existing activity on the same day
        Optional<DailyActivity> existing = activityRepository
                .findByUserIdAndActivityDate(userId, request.activityDate());

        DailyActivity activity;

        if (existing.isPresent()) {
            // Update existing activity (e.g., user first said NO_DREAM, then logged a dream)
            activity = existing.get();
            activity.setActivityType(request.activityType());
            activity.setGoalText(request.goalText());
            linkDreamIfPresent(activity, request);
        } else {
            // Create new activity
            activity = DailyActivity.builder()
                    .clientId(request.clientId())
                    .user(user)
                    .activityDate(request.activityDate())
                    .activityType(request.activityType())
                    .goalText(request.goalText())
                    .build();
            linkDreamIfPresent(activity, request);
        }

        activity = activityRepository.save(activity);

        // Update streak for this day
        streakService.recordActivity(userId);

        log.info("Activity logged: id={}, type={}", activity.getId(), activity.getActivityType());
        return toResponse(activity);
    }

    /**
     * Creates an activity during sync without triggering streak updates.
     * Streak will be recalculated separately after the full sync.
     * Each call runs in its own transaction for failure isolation.
     *
     * @param userId  The owner's user ID (re-loaded inside the new transaction).
     * @param request The activity creation payload.
     * @return The persisted DailyActivity entity.
     */
    @Transactional
    public DailyActivity logActivityForSync(Long userId, DailyActivityRequest request) {
        // Idempotency: check by client_id first
        Optional<DailyActivity> byClientId = activityRepository.findByClientId(request.clientId());
        if (byClientId.isPresent()) {
            return byClientId.get();
        }

        // Check for existing activity on that date
        Optional<DailyActivity> byDate = activityRepository
                .findByUserIdAndActivityDate(userId, request.activityDate());
        if (byDate.isPresent()) {
            return byDate.get();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        DailyActivity activity = DailyActivity.builder()
                .clientId(request.clientId())
                .user(user)
                .activityDate(request.activityDate())
                .activityType(request.activityType())
                .goalText(request.goalText())
                .build();
        linkDreamIfPresent(activity, request);

        return activityRepository.save(activity);
    }

    /**
     * Retrieves activities for a user within a date range.
     */
    @Transactional(readOnly = true)
    public List<DailyActivityResponse> getActivities(Long userId, LocalDate from, LocalDate to) {
        return activityRepository
                .findByUserIdAndActivityDateBetween(userId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Links a dream to the activity if a dream client ID is provided.
     */
    private void linkDreamIfPresent(DailyActivity activity, DailyActivityRequest request) {
        if (request.dreamClientId() != null && request.activityType() == ActivityType.DREAM) {
            Dream dream = dreamRepository.findByClientId(request.dreamClientId())
                    .orElse(null);
            activity.setDream(dream);
        }
    }

    private DailyActivityResponse toResponse(DailyActivity activity) {
        return new DailyActivityResponse(
                activity.getId(),
                activity.getClientId(),
                activity.getActivityDate(),
                activity.getActivityType(),
                activity.getDream() != null ? activity.getDream().getId() : null,
                activity.getGoalText(),
                activity.getCreatedAt()
        );
    }

}
