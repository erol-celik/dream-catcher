package com.dreamdiary.entity;

import com.dreamdiary.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Weekly AI-generated analysis report.
 * Triggered when the user logs their 7th dream (the "7th Dream" trigger).
 * The input_tags field contains only the extracted tag arrays, NOT raw dream text,
 * to minimize LLM token consumption during the weekly analysis prompt.
 */
@Entity
@Table(name = "weekly_reports", indexes = {
        @Index(name = "idx_weekly_reports_user_id_week", columnList = "user_id, week_start")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @Column(name = "input_tags", nullable = false, columnDefinition = "JSON")
    private String inputTags;

    @Column(name = "report_content", nullable = false, columnDefinition = "JSON")
    private String reportContent;

    @Column(name = "dream_count", nullable = false)
    private Integer dreamCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

}
