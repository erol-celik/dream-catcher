package com.dreamcatcher.service;

import com.dreamcatcher.dto.request.GuestRegistrationRequest;
import com.dreamcatcher.dto.response.GuestRegistrationResponse;
import com.dreamcatcher.dto.response.UserProfileResponse;
import com.dreamcatcher.entity.Device;
import com.dreamcatcher.entity.Notification;
import com.dreamcatcher.entity.Streak;
import com.dreamcatcher.entity.User;
import com.dreamcatcher.enums.NotificationType;
import com.dreamcatcher.exception.ResourceNotFoundException;
import com.dreamcatcher.repository.*;
import com.dreamcatcher.security.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service handling user registration, profile, and device management.
 * Supports the guest-first approach where users can operate without an account
 * and link their data to an authenticated account later.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final StreakRepository streakRepository;
    private final NotificationRepository notificationRepository;
    private final DreamRepository dreamRepository;
    private final JwtUtil jwtUtil;

    /**
     * Registers a new guest user and checks device fingerprint
     * for free trial eligibility.
     */
    @Transactional
    public GuestRegistrationResponse registerGuest(GuestRegistrationRequest request) {
        log.info("Registering guest user with device: {}", request.platform());

        // Check device fingerprint for previous trial usage
        Optional<Device> existingDevice = deviceRepository
                .findByDeviceFingerprint(request.deviceFingerprint());
        boolean freeTrialAvailable = existingDevice
                .map(d -> !d.getFreeTrialUsed())
                .orElse(true);

        // Create guest user
        User user = User.builder()
                .isGuest(true)
                .guestToken(UUID.randomUUID().toString())
                .isPremium(freeTrialAvailable)
                .premiumExpiresAt(freeTrialAvailable
                        ? LocalDateTime.now().plusDays(7)
                        : null)
                .build();
        user = userRepository.save(user);

        // Register or update device (with race condition protection)
        try {
            Device device;
            if (existingDevice.isPresent()) {
                device = existingDevice.get();
                device.setUser(user);
                device.setLastSeenAt(LocalDateTime.now());
                device.setAppVersion(request.appVersion());
            } else {
                device = Device.builder()
                        .user(user)
                        .deviceFingerprint(request.deviceFingerprint())
                        .platform(request.platform())
                        .appVersion(request.appVersion())
                        .firstSeenAt(LocalDateTime.now())
                        .lastSeenAt(LocalDateTime.now())
                        .freeTrialUsed(freeTrialAvailable)
                        .build();
            }
            if (freeTrialAvailable) {
                device.setFreeTrialUsed(true);
            }
            deviceRepository.save(device);
        } catch (DataIntegrityViolationException e) {
            // Race condition: another request registered this fingerprint concurrently.
            // Re-fetch the existing device and link it to the new user.
            log.warn("Device fingerprint race condition detected, re-fetching: {}",
                    request.deviceFingerprint());
            Device existingDev = deviceRepository
                    .findByDeviceFingerprint(request.deviceFingerprint())
                    .orElseThrow(() -> new RuntimeException(
                            "Device vanished after race condition"));
            existingDev.setUser(user);
            existingDev.setLastSeenAt(LocalDateTime.now());
            deviceRepository.save(existingDev);
        }

        // Initialize streak record
        Streak streak = Streak.builder()
                .user(user)
                .currentStreak(0)
                .longestStreak(0)
                .build();
        streakRepository.save(streak);

        // Create default notification preferences
        createDefaultNotifications(user);

        log.info("Guest user registered: id={}, trial={}", user.getId(), freeTrialAvailable);

        String token = jwtUtil.generateToken(user.getId());

        return new GuestRegistrationResponse(
                user.getId(),
                user.getGuestToken(),
                token,
                freeTrialAvailable
        );
    }

    /**
     * Retrieves user profile with summary stats.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserById(userId);

        long totalDreams = dreamRepository.countByUserId(userId);
        int currentStreak = streakRepository.findByUserId(userId)
                .map(Streak::getCurrentStreak)
                .orElse(0);

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getIsGuest(),
                user.getIsPremium(),
                user.getPremiumExpiresAt(),
                (int) totalDreams,
                currentStreak
        );
    }

    /**
     * Finds a user by ID or throws ResourceNotFoundException.
     */
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Finds a user by guest token or throws ResourceNotFoundException.
     */
    public User findByGuestToken(String guestToken) {
        return userRepository.findByGuestToken(guestToken)
                .orElseThrow(() -> new ResourceNotFoundException("User", "guestToken", guestToken));
    }

    /**
     * Creates default notification preferences for a new user.
     */
    private void createDefaultNotifications(User user) {
        // Morning reminder: 07:30 — "Write before it fades"
        notificationRepository.save(Notification.builder()
                .user(user)
                .notificationType(NotificationType.MORNING_REMINDER)
                .isEnabled(true)
                .scheduledTime(LocalTime.of(7, 30))
                .build());

        // Evening reminder: 21:00 — "Ready for tonight?"
        notificationRepository.save(Notification.builder()
                .user(user)
                .notificationType(NotificationType.EVENING_REMINDER)
                .isEnabled(true)
                .scheduledTime(LocalTime.of(21, 0))
                .build());

        // Weekly report notification
        notificationRepository.save(Notification.builder()
                .user(user)
                .notificationType(NotificationType.WEEKLY_REPORT)
                .isEnabled(true)
                .build());
    }

}
