-- Add new score_strategy column
ALTER TABLE leaderboards ADD COLUMN score_strategy VARCHAR(20) NOT NULL DEFAULT 'BEST_ONLY';

-- Migrate existing data based on boolean flags
UPDATE leaderboards SET score_strategy = 
    CASE 
        WHEN is_cumulative = true THEN 'CUMULATIVE'
        WHEN allow_multiple_scores = true THEN 'MULTIPLE_ENTRIES'
        ELSE 'BEST_ONLY'
    END;

-- Drop old boolean columns
ALTER TABLE leaderboards DROP COLUMN allow_multiple_scores;
ALTER TABLE leaderboards DROP COLUMN is_cumulative;
