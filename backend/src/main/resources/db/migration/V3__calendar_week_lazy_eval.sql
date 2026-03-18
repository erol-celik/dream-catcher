-- ===========================================================================
-- V3: Calendar-Based Weekly Reports & Lazy Evaluation
-- Modifies the weekly_reports table to support the new INSUFFICIENT_DATA
-- status when the user logs fewer than 3 dreams in a calendar week.
-- ===========================================================================

-- 1. Make input_tags and report_content nullable
ALTER TABLE weekly_reports MODIFY COLUMN input_tags JSON NULL;
ALTER TABLE weekly_reports MODIFY COLUMN report_content JSON NULL;

-- 2. Add INSUFFICIENT_DATA to the status ENUM
ALTER TABLE weekly_reports
    MODIFY COLUMN status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'INSUFFICIENT_DATA') NOT NULL DEFAULT 'PENDING';
