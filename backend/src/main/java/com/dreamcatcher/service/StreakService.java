package com.dreamcatcher.service;

import com.dreamcatcher.dto.response.StreakResponse;
import com.dreamcatcher.entity.Streak;
import com.dreamcatcher.entity.User;
import com.dreamcatcher.repository.StreakRepository;
import com.dreamcatcher.repository.UserRepository;
import com.dreamcatcher.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service managing the gamified streak system.
 * Streak rules:
 * - Consecutive daily activity increments the streak.
 * - Missing a day resets the streak to 1.
 * - Multiple activities on the same day do not double-count.
 * - "I didn't dream" and "daily goal" activities also count for the streak.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StreakService {

    private final StreakRepository streakRepository;
    private final UserRepository userRepository;

    /**
     * Records user activity for today and updates the streak accordingly.
     */
    @Transactional
    public void recordActivity(Long userId) {
        Streak streak = getOrCreateStreak(userId);
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = streak.getLastActivityDate();

        if (lastActivity != null && lastActivity.equals(today)) {
            // Already logged today — no streak change
            log.debug("Streak already updated today for user={}", userId);
            return;
        }

        if (lastActivity != null && lastActivity.equals(today.minusDays(1))) {
            // Consecutive day: increment
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            // First activity or broken streak: reset to 1
            streak.setCurrentStreak(1);
        }

        // Update longest streak if current exceeds it
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streak.setLastActivityDate(today);
        streakRepository.save(streak);

        log.info("Streak updated for user={}: current={}, longest={}",
                userId, streak.getCurrentStreak(), streak.getLongestStreak());
    }

    /**
     * Retrieves the streak information for a user.
     */
    @Transactional(readOnly = true)
    public StreakResponse getStreak(Long userId) {
        Streak streak = getOrCreateStreak(userId);
        boolean activeToday = streak.getLastActivityDate() != null
                && streak.getLastActivityDate().equals(LocalDate.now());

        return new StreakResponse(
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getLastActivityDate(),
                activeToday
        );
    }

    /**
     * Finds an existing streak or creates a new one for the user.
     */
    private Streak getOrCreateStreak(Long userId) {
        return streakRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Streak newStreak = Streak.builder()
                            .user(user)
                            .currentStreak(0)
                            .longestStreak(0)
                            .build();
                    return streakRepository.save(newStreak);
                });
    }

}
