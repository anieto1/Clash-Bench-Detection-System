-- ============================================================================
-- Clash of Clans CWL Tracker - Database Schema
-- PostgreSQL 16+
-- ============================================================================

-- ============================================================================
-- BASE ENTITIES
-- These represent the core game objects that exist independently
-- ============================================================================

-- Clans are the primary organizational unit
-- We track clans that our users want to monitor
CREATE TABLE clan
(
    tag         VARCHAR(15) PRIMARY KEY, -- e.g., '#2Y28CGP8' (API includes #)
    name        VARCHAR(50) NOT NULL,
    clan_level  INTEGER     NOT NULL DEFAULT 1, -- Clan experience level
    clan_points INTEGER     NOT NULL DEFAULT 0,
    war_wins    INTEGER     NOT NULL DEFAULT 0,
    war_ties    INTEGER     NOT NULL DEFAULT 0,
    war_losses  INTEGER     NOT NULL DEFAULT 0,
    description TEXT,
    badge_url   VARCHAR(255),            -- Clan badge image
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Players belong to clans (or may be clanless)
-- This is the "current state" of the player
CREATE TABLE player
(
    tag                VARCHAR(15) PRIMARY KEY,        -- e.g., '#ABC123XYZ'
    name               VARCHAR(50) NOT NULL,
    town_hall_level    SMALLINT    NOT NULL CHECK (town_hall_level BETWEEN 1 AND 17),
    clan_tag           VARCHAR(15) REFERENCES clan (tag) ON DELETE SET NULL,
    clan_role          VARCHAR(20),                    -- 'leader', 'coLeader', 'elder', 'member'
    war_stars          INTEGER     NOT NULL DEFAULT 0, -- All-time war stars
    donations          INTEGER     NOT NULL DEFAULT 0,
    donations_received INTEGER     NOT NULL DEFAULT 0,
    exp_level          INTEGER     NOT NULL DEFAULT 1,
    trophies           INTEGER     NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_player_clan ON player (clan_tag);
CREATE INDEX idx_player_town_hall ON player (town_hall_level);


-- ============================================================================
-- CURRENT STATE (Normalized)
-- These tables track the player's current troop/hero/spell/pet levels
-- They get UPDATED when we poll the API - always reflects latest state
-- ============================================================================

-- Heroes: Barbarian King, Archer Queen, Grand Warden, Royal Champion, Minion Prince
CREATE TABLE player_hero
(
    player_tag VARCHAR(15) NOT NULL REFERENCES player (tag) ON DELETE CASCADE,
    name       VARCHAR(30) NOT NULL, -- e.g., 'Barbarian King'
    level      SMALLINT    NOT NULL CHECK (level >= 0),
    max_level  SMALLINT    NOT NULL CHECK (max_level >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (player_tag, name)
);

-- Equipment attached to heroes (e.g., Giant Gauntlet on Barbarian King)
CREATE TABLE player_equipment
(
    player_tag VARCHAR(15) NOT NULL REFERENCES player (tag) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL, -- e.g., 'Giant Gauntlet'
    hero_name  VARCHAR(30) NOT NULL, -- Which hero this equipment belongs to
    level      SMALLINT    NOT NULL CHECK (level >= 0),
    max_level  SMALLINT    NOT NULL CHECK (max_level >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (player_tag, name)
);

CREATE INDEX idx_player_equipment_hero ON player_equipment (player_tag, hero_name);

-- Troops (Barbarian, Archer, Dragon, etc.)
CREATE TABLE player_troop
(
    player_tag VARCHAR(15) NOT NULL REFERENCES player (tag) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    level      SMALLINT    NOT NULL CHECK (level >= 0),
    max_level  SMALLINT    NOT NULL CHECK (max_level >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (player_tag, name)
);

-- Spells (Lightning, Heal, Rage, etc.)
CREATE TABLE player_spell
(
    player_tag VARCHAR(15) NOT NULL REFERENCES player (tag) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    level      SMALLINT    NOT NULL CHECK (level >= 0),
    max_level  SMALLINT    NOT NULL CHECK (max_level >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (player_tag, name)
);

-- Pets (L.A.S.S.I, Mighty Yak, etc.) - up to 11 total
CREATE TABLE player_pet
(
    player_tag VARCHAR(15) NOT NULL REFERENCES player (tag) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    level      SMALLINT    NOT NULL CHECK (level >= 0),
    max_level  SMALLINT    NOT NULL CHECK (max_level >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (player_tag, name)
);


-- ============================================================================
-- CWL ENTITIES
-- These track Clan War League seasons, wars, and attacks
-- Historical data that never changes once recorded
-- ============================================================================

-- A CWL season for a specific clan (occurs monthly)
CREATE TABLE cwl_season
(
    clan_tag          VARCHAR(15)   NOT NULL REFERENCES clan (tag) ON DELETE CASCADE,
    season            VARCHAR(7)    NOT NULL,
    league_name       VARCHAR(50),
    final_placement   SMALLINT CHECK (final_placement BETWEEN 1 AND 8),
    total_stars       INTEGER       NOT NULL DEFAULT 0,
    total_destruction DECIMAL(5, 2) NOT NULL DEFAULT 0,
    is_completed      BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),


    PRIMARY KEY (clan_tag, season)
);


CREATE INDEX idx_cwl_season_clan ON cwl_season (clan_tag);
CREATE INDEX idx_cwl_season_date ON cwl_season (season DESC);

-- Participants in a CWL season (the roster, up to 35 players)
-- Contains the SNAPSHOT of their stats at the time of the season
CREATE TABLE cwl_participant
(
    clan_tag       VARCHAR(15) NOT NULL,
    season         VARCHAR(7)  NOT NULL,
    player_tag     VARCHAR(15) NOT NULL REFERENCES player (tag) ON DELETE CASCADE,
    stats_snapshot JSONB       NOT NULL,
    /*
        Example stats_snapshot structure:
        {
            "townHallLevel": 16,
            "heroes": [
                {"name": "Barbarian King", "level": 85, "maxLevel": 95},
                {"name": "Archer Queen", "level": 90, "maxLevel": 95},
                {"name": "Grand Warden", "level": 70, "maxLevel": 75},
                {"name": "Royal Champion", "level": 45, "maxLevel": 50},
                {"name": "Minion Prince", "level": 30, "maxLevel": 40}
            ],
            "equipment": [
                {"name": "Giant Gauntlet", "level": 18, "maxLevel": 27, "hero": "Barbarian King"},
                {"name": "Frozen Arrow", "level": 15, "maxLevel": 27, "hero": "Archer Queen"}
            ],
            "pets": [
                {"name": "L.A.S.S.I", "level": 15, "maxLevel": 15},
                {"name": "Mighty Yak", "level": 15, "maxLevel": 15}
            ]
        }
    */
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (clan_tag, season, player_tag),
    FOREIGN KEY (clan_tag, season)
        REFERENCES cwl_season (clan_tag, season)
        ON DELETE CASCADE
);

CREATE INDEX idx_cwl_participant_player ON cwl_participant (player_tag);

-- Individual CWL war (one per day, 7 total per season)
CREATE TABLE cwl_war
(
    war_tag              VARCHAR(50) PRIMARY KEY, -- API war tag for this specific war
    clan_tag             VARCHAR(15)   NOT NULL,
    season               VARCHAR(7)    NOT NULL,
    day_number           SMALLINT      NOT NULL CHECK (day_number BETWEEN 1 AND 7),

    -- Opponent info
    opponent_clan_tag    VARCHAR(15)   NOT NULL,
    opponent_clan_name   VARCHAR(50)   NOT NULL,
    opponent_clan_level  INTEGER,

    -- Results
    our_stars            SMALLINT      NOT NULL DEFAULT 0,
    our_destruction      DECIMAL(5, 2) NOT NULL DEFAULT 0,
    opponent_stars       SMALLINT      NOT NULL DEFAULT 0,
    opponent_destruction DECIMAL(5, 2) NOT NULL DEFAULT 0,
    result               VARCHAR(4) CHECK (result IN ('WIN', 'LOSE', 'TIE')),

    -- War timing
    start_time           TIMESTAMPTZ,
    end_time             TIMESTAMPTZ,

    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    UNIQUE (clan_tag, season, day_number),
    FOREIGN KEY (clan_tag, season)
        REFERENCES cwl_season (clan_tag, season)
        ON DELETE CASCADE
);

CREATE INDEX idx_cwl_war_season ON cwl_war (clan_tag, season);

-- War members: the 15 players participating in a specific day's war
CREATE TABLE cwl_war_member
(
    war_tag      VARCHAR(50) NOT NULL REFERENCES cwl_war (war_tag) ON DELETE CASCADE,
    player_tag   VARCHAR(15) NOT NULL REFERENCES player (tag) ON DELETE CASCADE,
    map_position SMALLINT    NOT NULL CHECK (map_position BETWEEN 1 AND 15),
    attacked     BOOLEAN     NOT NULL DEFAULT FALSE,

    PRIMARY KEY (war_tag, player_tag),
    UNIQUE (war_tag, map_position)
);


CREATE INDEX idx_cwl_war_member_player ON cwl_war_member (player_tag);

-- Individual attacks in a CWL war
CREATE TABLE cwl_attack
(
    war_tag                VARCHAR(50)   NOT NULL REFERENCES cwl_war (war_tag) ON DELETE CASCADE,
    attacker_tag           VARCHAR(15)   NOT NULL REFERENCES player (tag) ON DELETE CASCADE,
    attacker_map_position  SMALLINT      NOT NULL CHECK (attacker_map_position BETWEEN 1 AND 15),

    defender_tag           VARCHAR(15)   NOT NULL,
    defender_name          VARCHAR(50),
    defender_th_level      SMALLINT      NOT NULL CHECK (defender_th_level BETWEEN 1 AND 17),
    defender_map_position  SMALLINT      NOT NULL CHECK (defender_map_position BETWEEN 1 AND 15),

    stars                  SMALLINT      NOT NULL CHECK (stars BETWEEN 0 AND 3),
    destruction_percentage DECIMAL(5, 2) NOT NULL CHECK (destruction_percentage BETWEEN 0 AND 100),
    attack_order           SMALLINT,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    PRIMARY KEY (war_tag, attacker_tag)
);


CREATE INDEX idx_cwl_attack_war ON cwl_attack (war_tag);
CREATE INDEX idx_cwl_attack_attacker ON cwl_attack (attacker_tag);


-- ============================================================================
-- TRACKING/SYNC METADATA
-- Helps us know what we've polled and when
-- ============================================================================

-- Clans we're actively tracking (polling for war data)
CREATE TABLE tracked_clan
(
    clan_tag       VARCHAR(15) PRIMARY KEY REFERENCES clan (tag) ON DELETE CASCADE,
    is_active      BOOLEAN     NOT NULL DEFAULT TRUE,
    last_polled_at TIMESTAMPTZ,
    poll_interval  INTEGER     NOT NULL DEFAULT 300, -- seconds between polls (5 min default)
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- ============================================================================
-- HELPER FUNCTIONS
-- ============================================================================

-- Auto-update the updated_at timestamp
CREATE
OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at
= NOW();
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Apply trigger to tables with updated_at
CREATE TRIGGER update_clan_updated_at
    BEFORE UPDATE
    ON clan
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_player_updated_at
    BEFORE UPDATE
    ON player
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cwl_season_updated_at
    BEFORE UPDATE
    ON cwl_season
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cwl_war_updated_at
    BEFORE UPDATE
    ON cwl_war
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


-- ============================================================================
-- USEFUL VIEWS
-- ============================================================================

-- Player's total CWL stats across all seasons
CREATE VIEW player_cwl_stats AS
SELECT p.tag,
       p.name,
       p.town_hall_level,
       COUNT(DISTINCT (cwp.clan_tag, cwp.season))                  AS seasons_participated,
       COUNT(ca.attacker_tag)                                      AS total_attacks,
       COALESCE(SUM(ca.stars), 0)                                  AS total_stars,
       COALESCE(AVG(ca.destruction_percentage), 0)                 AS avg_destruction,
       COALESCE(SUM(CASE WHEN ca.stars = 3 THEN 1 ELSE 0 END), 0) AS three_star_count,
       COUNT(cwm.player_tag) - COUNT(ca.attacker_tag)              AS missed_attacks
FROM player p
         LEFT JOIN cwl_participant cwp ON p.tag = cwp.player_tag
         LEFT JOIN cwl_war_member cwm ON p.tag = cwm.player_tag
         LEFT JOIN cwl_attack ca ON p.tag = ca.attacker_tag AND cwm.war_tag = ca.war_tag
GROUP BY p.tag, p.name, p.town_hall_level;

-- CWL season summary with all wars
CREATE VIEW cwl_season_summary AS
SELECT cs.clan_tag,
       cs.season,
       c.name                                              AS clan_name,
       cs.league_name,
       cs.final_placement,
       COUNT(DISTINCT cw.war_tag)                          AS wars_played,
       SUM(CASE WHEN cw.result = 'WIN' THEN 1 ELSE 0 END)  AS wins,
       SUM(CASE WHEN cw.result = 'LOSE' THEN 1 ELSE 0 END) AS losses,
       SUM(CASE WHEN cw.result = 'TIE' THEN 1 ELSE 0 END)  AS ties,
       SUM(cw.our_stars)                                   AS total_stars,
       AVG(cw.our_destruction)                             AS avg_destruction
FROM cwl_season cs
         JOIN clan c ON cs.clan_tag = c.tag
         LEFT JOIN cwl_war cw ON cs.clan_tag = cw.clan_tag AND cs.season = cw.season
GROUP BY cs.clan_tag, cs.season, c.name, cs.league_name, cs.final_placement;
