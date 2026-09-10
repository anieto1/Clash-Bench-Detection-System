-- Update constraints for TH 18+ and CWL 30v30 wars
-- Town hall levels: allow up to 20 (future-proofing)
ALTER TABLE player DROP CONSTRAINT IF EXISTS player_town_hall_level_check;
ALTER TABLE player ADD CONSTRAINT player_town_hall_level_check CHECK (town_hall_level BETWEEN 1 AND 20);

ALTER TABLE cwl_attack DROP CONSTRAINT IF EXISTS cwl_attack_defender_th_level_check;
ALTER TABLE cwl_attack ADD CONSTRAINT cwl_attack_defender_th_level_check CHECK (defender_th_level BETWEEN 1 AND 20);

-- Map positions: CWL can be up to 30v30
ALTER TABLE cwl_war_member DROP CONSTRAINT IF EXISTS cwl_war_member_map_position_check;
ALTER TABLE cwl_war_member ADD CONSTRAINT cwl_war_member_map_position_check CHECK (map_position BETWEEN 1 AND 50);

ALTER TABLE cwl_attack DROP CONSTRAINT IF EXISTS cwl_attack_attacker_map_position_check;
ALTER TABLE cwl_attack ADD CONSTRAINT cwl_attack_attacker_map_position_check CHECK (attacker_map_position BETWEEN 1 AND 50);

ALTER TABLE cwl_attack DROP CONSTRAINT IF EXISTS cwl_attack_defender_map_position_check;
ALTER TABLE cwl_attack ADD CONSTRAINT cwl_attack_defender_map_position_check CHECK (defender_map_position BETWEEN 1 AND 50);
