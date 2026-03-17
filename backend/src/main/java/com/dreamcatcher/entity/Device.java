package com.dreamcatcher.entity;

import com.dreamcatcher.enums.Platform;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Device registration entity for abuse prevention.
 * Tracks device fingerprints to prevent users from bypassing
 * the 7-day free premium trial by creating new accounts on the same device.
 */
@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_devices_fingerprint", columnList = "device_fingerprint", unique = true),
        @Index(name = "idx_devices_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "device_fingerprint", nullable = false, unique = true, length = 512)
    private String deviceFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "free_trial_used", nullable = false)
    @Builder.Default
    private Boolean freeTrialUsed = false;

}
