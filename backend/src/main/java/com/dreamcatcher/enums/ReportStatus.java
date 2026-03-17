package com.dreamcatcher.enums;

/**
 * Processing status for weekly AI analysis reports.
 * Tracks the lifecycle of a report from creation to completion.
 */
public enum ReportStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
