package com.dreamcatcher.enums;

/**
 * Types of daily user activity for streak tracking.
 * DREAM: user logged a dream entry.
 * NO_DREAM: user did not dream or does not remember (streak preserved).
 * GOAL: user answered the daily goal question (streak preserved).
 */
public enum ActivityType {
    DREAM,
    NO_DREAM,
    GOAL
}
