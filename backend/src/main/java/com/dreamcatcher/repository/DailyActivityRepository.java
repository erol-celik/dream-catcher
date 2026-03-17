package com.dreamcatcher.repository;

import com.dreamcatcher.entity.DailyActivity;
import com.dreamcatcher.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyActivity entity operations.
 * Supports date-based lookups and activity type counting for streaks and analytics.
 */
@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long> {

    Optional<DailyActivity> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);

    Optional<DailyActivity> findByClientId(String clientId);

    List<DailyActivity> findByUserIdAndActivityDateBetween(Long userId, LocalDate start, LocalDate end);

    long countByUserIdAndActivityType(Long userId, ActivityType activityType);

    boolean existsByUserIdAndActivityDate(Long userId, LocalDate activityDate);

}
