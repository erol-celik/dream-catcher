package com.dreamdiary.entity;

import com.dreamdiary.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily activity log for the habit loop system.
 * Each user can have exactly one activity per day (composite unique constraint).
 * When a user selects "I did not dream", a NO_DREAM activity is created
 * and a dummy question is asked to maintain the daily engagement habit.
 */
@Entity
@Table(name = "daily_activities",
        indexes = {
                @Index(name = "idx_daily_activities_user_date", columnList = "user_id, activity_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_daily_activities_user_date", columnNames = {"user_id", "activity_date"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true, length = 36)
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dream_id")
    private Dream dream;

    @Column(name = "goal_text", length = 500)
    private String goalText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
