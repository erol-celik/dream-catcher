package com.dreamcatcher.repository;

import com.dreamcatcher.entity.Notification;
import com.dreamcatcher.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Notification entity operations.
 * Supports lookup by user and notification type for preference management.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    Optional<Notification> findByUserIdAndNotificationType(Long userId, NotificationType notificationType);

    List<Notification> findByUserIdAndIsEnabledTrue(Long userId);

}
