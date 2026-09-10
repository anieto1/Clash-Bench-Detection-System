-- Add town_hall_level to cwl_war_member so scoring uses per-day TH, not season snapshot
ALTER TABLE cwl_war_member ADD COLUMN town_hall_level SMALLINT;

-- Backfill existing rows from the attacker's cwl_attack record if available
UPDATE cwl_war_member wm
SET town_hall_level = p.town_hall_level
FROM player p
WHERE p.tag = wm.player_tag
  AND wm.town_hall_level IS NULL;

-- Default any remaining nulls to 1 (shouldn't happen, but safe)
UPDATE cwl_war_member SET town_hall_level = 1 WHERE town_hall_level IS NULL;

-- Now make it NOT NULL with a constraint
ALTER TABLE cwl_war_member ALTER COLUMN town_hall_level SET NOT NULL;
ALTER TABLE cwl_war_member ADD CONSTRAINT cwl_war_member_town_hall_level_check CHECK (town_hall_level BETWEEN 1 AND 20);
