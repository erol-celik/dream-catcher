-- ===========================================================================
-- V2: Add NIGHTMARE and GREAT to dream_sentiments sentiment ENUM
-- These values are client-facing sentiments selected by the user in the
-- mobile app, in addition to the AI-generated categories.
-- ===========================================================================

ALTER TABLE dream_sentiments
    MODIFY COLUMN sentiment ENUM('POSITIVE', 'NEGATIVE', 'NEUTRAL', 'MIXED', 'NIGHTMARE', 'GREAT') NOT NULL;
