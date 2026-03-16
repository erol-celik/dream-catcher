package com.dreamdiary.repository;

import com.dreamdiary.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Streak entity operations.
 * Each user has exactly one streak record.
 */
@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {

    Optional<Streak> findByUserId(Long userId);

}
