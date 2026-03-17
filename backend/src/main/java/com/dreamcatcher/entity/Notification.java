package com.dreamcatcher.entity;

import com.dreamcatcher.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * User notification preference entity.
 * Controls which statistical time-based local notifications the user receives.
 * No background sensor tracking; notifications are purely schedule-driven.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

}
