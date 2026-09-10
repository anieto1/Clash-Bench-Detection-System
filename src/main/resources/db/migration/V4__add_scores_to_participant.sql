-- Add CBDS score columns to cwl_participant so season performance is persisted
-- These are populated after leaderboard calculation during sync

ALTER TABLE cwl_participant ADD COLUMN total_score        INTEGER;
ALTER TABLE cwl_participant ADD COLUMN attacks_made       SMALLINT;
ALTER TABLE cwl_participant ADD COLUMN attacks_missed     SMALLINT;
ALTER TABLE cwl_participant ADD COLUMN average_stars      DECIMAL(4, 2);
ALTER TABLE cwl_participant ADD COLUMN average_destruction DECIMAL(5, 2);
ALTER TABLE cwl_participant ADD COLUMN town_hall_level    SMALLINT;
ALTER TABLE cwl_participant ADD COLUMN scores_computed_at TIMESTAMPTZ;

-- Index for querying player history across all seasons
CREATE INDEX idx_cwl_participant_scores ON cwl_participant (player_tag, season DESC)
    WHERE total_score IS NOT NULL;
