package com.dreamcatcher.repository;

import com.dreamcatcher.entity.WeeklyReport;
import com.dreamcatcher.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WeeklyReport entity operations.
 * Supports status-based queries for the asynchronous report generation pipeline.
 */
@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    List<WeeklyReport> findByUserIdOrderByWeekStartDesc(Long userId);

    Optional<WeeklyReport> findByUserIdAndWeekStart(Long userId, LocalDate weekStart);

    List<WeeklyReport> findByStatus(ReportStatus status);

    boolean existsByUserIdAndWeekStart(Long userId, LocalDate weekStart);

}
