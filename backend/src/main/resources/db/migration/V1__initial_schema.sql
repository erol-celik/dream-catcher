-- ===========================================================================
-- Dream Diary - Initial Database Schema
-- Version: V1
-- Description: Creates all core tables for the Dream Diary application.
-- Engine: MySQL 8.x with InnoDB and utf8mb4 charset.
-- ===========================================================================

-- -------------------------------------------------------
-- 1. Users table
-- Supports both guest and authenticated accounts.
-- -------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    display_name VARCHAR(100),
    auth_provider ENUM('GOOGLE', 'APPLE'),
    auth_provider_id VARCHAR(255),
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    premium_expires_at DATETIME,
    is_guest BOOLEAN NOT NULL DEFAULT TRUE,
    guest_token VARCHAR(255) UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_email (email),
    INDEX idx_users_auth_provider_id (auth_provider_id),
    INDEX idx_users_guest_token (guest_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -------------------------------------------------------
-- 2. Devices table
-- Tracks device fingerprints for free trial abuse prevention.
-- -------------------------------------------------------
CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    device_fingerprint VARCHAR(512) NOT NULL UNIQUE,
    platform ENUM('IOS', 'ANDROID') NOT NULL,
    app_version VARCHAR(20),
    first_seen_at DATETIME NOT NULL,
    last_seen_at DATETIME NOT NULL,
    free_trial_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_devices_user_id (user_id),
    CONSTRAINT fk_devices_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -------------------------------------------------------
-- 3. Dreams table
-- Each row is a single dream entry logged by a user.
-- client_id ensures idempotent sync from the mobile app.
-- -------------------------------------------------------
CREATE TABLE dreams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    word_count INT NOT NULL,
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,
    dream_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_dreams_user_id_dream_date (user_id, dream_date),
    CONSTRAINT fk_dreams_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -------------------------------------------------------
-- 4. Dream tags table
-- AI-extracted keywords from each dream entry.
-- Used as input for weekly analysis (instead of raw text).
-- -------------------------------------------------------
CREATE TABLE dream_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dream_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_dream_tags_dream_id (dream_id),
    INDEX idx_dream_tags_tag (tag),
    CONSTRAINT fk_dream_tags_dream FOREIGN KEY (dream_id) REFERENCES dreams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -------------------------------------------------------
-- 5. Dream sentiments table
-- One sentiment per dream (1:1 relationship).
-- Stores AI confidence score and raw response for auditing.
-- -------------------------------------------------------
CREATE TABLE dream_sentiments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dream_id BIGINT NOT NULL UNIQUE,
    sentiment ENUM('POSITIVE', 'NEGATIVE', 'NEUTRAL', 'MIXED') NOT NULL,
    confidence DECIMAL(3,2),
    raw_ai_response JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_dream_sentiments_dream_id (dream_id),
    CONSTRAINT fk_dream_sentiments_dream FOREIGN KEY (dream_id) REFERENCES dreams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -------------------------------------------------------
-- 6. Streaks table
-- Gamification: one streak record per user.
-- -------------------------------------------------------
CREATE TABLE streaks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_activity_date DATE,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_streaks_user_id (user_id),
    CONSTRAINT fk_streaks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -------------------------------------------------------
-- 7. Daily activities table
-- Tracks the daily habit loop (DREAM / NO_DREAM / GOAL).
-- Composite unique: one activity per user per day.
-- -------------------------------------------------------
CREATE TABLE daily_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    activity_date DATE NOT NULL,
    activity_type ENUM('DREAM', 'NO_DREAM', 'GOAL') NOT NULL,
    dream_id BIGINT,
    goal_text VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_daily_activities_user_date (user_id, activity_date),
    INDEX idx_daily_activities_user_date (user_id, activity_date),
    CONSTRAINT fk_daily_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_daily_activities_dream FOREIGN KEY (dream_id) REFERENCES dreams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -------------------------------------------------------
-- 8. Weekly reports table
-- AI-generated weekly analysis triggered on the 7th dream.
-- input_tags stores extracted tag arrays (NOT raw dream text).
-- -------------------------------------------------------
CREATE TABLE weekly_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    week_start DATE NOT NULL,
    week_end DATE NOT NULL,
    input_tags JSON NOT NULL,
    report_content JSON NOT NULL,
    dream_count INT NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_weekly_reports_user_id_week (user_id, week_start),
    CONSTRAINT fk_weekly_reports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- -------------------------------------------------------
-- 9. Notifications table
-- User preferences for time-based local push notifications.
-- -------------------------------------------------------
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type ENUM('MORNING_REMINDER', 'EVENING_REMINDER', 'WEEKLY_REPORT', 'COMMUNITY') NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    scheduled_time TIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_notifications_user_id (user_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
