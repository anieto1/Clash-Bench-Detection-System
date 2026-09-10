# Clash of Clans API Data Mapping

This document maps CoC API responses to our domain model, defines the polling workflow, and provides Java record definitions for JSON deserialization.

---

## Table of Contents

1. [API Endpoints Overview](#api-endpoints-overview)
2. [Polling Workflow](#polling-workflow)
3. [Response DTOs (Java Records)](#response-dtos-java-records)
4. [API to Database Mapping](#api-to-database-mapping)
5. [Data Filtering Rules](#data-filtering-rules)
6. [Error Handling](#error-handling)

---

## API Endpoints Overview

| Endpoint | Purpose | When to Call |
|----------|---------|--------------|
| `GET /clans/{clanTag}` | Clan info + member list | On tracking registration, periodic sync |
| `GET /players/{playerTag}` | Full player profile (heroes, troops, etc.) | When player attacks (for snapshot), periodic sync |
| `GET /clans/{clanTag}/currentwar/leaguegroup` | CWL group info (8 clans, 7 rounds, war tags) | When CWL starts, daily during CWL |
| `GET /clanwarleagues/wars/{warTag}` | Specific CWL war details (attacks, positions) | Every 2-5 min during active war day |

### Base URL
```
https://api.clashofclans.com/v1
```

### Authentication
```
Authorization: Bearer {API_KEY}
```

### Tag Encoding
Player and clan tags start with `#` which must be URL-encoded as `%23`:
```
#J9PCJY88 → %23J9PCJY88
```

---

## Polling Workflow

### Scenario 1: User Registers a Clan to Track

```
1. GET /clans/{clanTag}
   └── Store clan info
   └── For each member in memberList:
       └── GET /players/{memberTag}
           └── Store player + current heroes/troops/spells/pets
```

### Scenario 2: CWL Season Starts (Day 1 Prep)

```
1. GET /clans/{clanTag}/currentwar/leaguegroup
   └── Create cwl_season record
   └── Store all 8 participating clans
   └── Store all war tags for 7 rounds
   └── For each member in our clan's roster:
       └── Create cwl_participant record
       └── GET /players/{memberTag}
           └── Create stats_snapshot JSON
```

### Scenario 3: During Active War Day (Poll Every 2-5 Min)

```
1. GET /clanwarleagues/wars/{todaysWarTag}
   └── If state = "inWar" or "warEnded":
       └── Update/create cwl_war record
       └── For each member in our clan:
           └── Update cwl_war_member (map position)
           └── If member has attack AND not already recorded:
               └── Create cwl_attack record
               └── GET /players/{memberTag}  (if snapshot not exists)
                   └── Update cwl_participant.stats_snapshot
```

### Scenario 4: War Day Ends

```
1. Final poll of GET /clanwarleagues/wars/{warTag}
   └── Update cwl_war with final stars/destruction/result
   └── Mark any members without attacks as attacked=false
   └── Move to next day's war tag
```

### Scenario 5: CWL Season Ends (After Day 7)

```
1. GET /clans/{clanTag}/currentwar/leaguegroup
   └── Update cwl_season with final_placement, is_completed=true
```

---

## Response DTOs (Java Records)

All records use Jackson annotations. We filter for `village: "home"` to exclude Builder Base data.

### Common Components

```java
package com.coctracker.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Badge/icon URLs provided by the API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BadgeUrls(
    String small,
    String medium,
    String large
) {}

/**
 * Base structure for items with name, level, maxLevel.
 * Used for troops, spells, heroes, equipment, pets.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LeveledItem(
    String name,
    Integer level,
    Integer maxLevel,
    String village,
    @JsonProperty("superTroopIsActive") Boolean superTroopIsActive
) {
    public boolean isHomeVillage() {
        return "home".equals(village);
    }
}

/**
 * Equipment item - nested under heroes or in flat heroEquipment array.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Equipment(
    String name,
    Integer level,
    Integer maxLevel,
    String village
) {}
```

### Player Endpoint Response

**Endpoint:** `GET /players/{playerTag}`

```java
package com.coctracker.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Clan summary embedded in player response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerClanInfo(
    String tag,
    String name,
    Integer clanLevel,
    BadgeUrls badgeUrls
) {}

/**
 * Hero with equipped items.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Hero(
    String name,
    Integer level,
    Integer maxLevel,
    String village,
    List<Equipment> equipment  // Currently equipped (2 slots)
) {
    public boolean isHomeVillage() {
        return "home".equals(village);
    }
}

/**
 * Full player profile response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerResponse(
    String tag,
    String name,
    Integer townHallLevel,
    @JsonProperty("townHallWeaponLevel") Integer townHallWeaponLevel,
    Integer expLevel,
    Integer trophies,
    Integer warStars,
    Integer donations,
    Integer donationsReceived,
    String role,                    // "member", "elder", "coLeader", "leader"
    String warPreference,           // "in" or "out"
    PlayerClanInfo clan,
    List<Hero> heroes,              // Includes equipped items
    List<LeveledItem> troops,       // Mixed: regular troops, super troops, siege, AND pets
    List<LeveledItem> spells,
    List<Equipment> heroEquipment   // All owned equipment (not used - we use nested)
) {
    
    /**
     * Filter heroes to home village only (excludes Battle Machine, Battle Copter).
     */
    public List<Hero> homeHeroes() {
        if (heroes == null) return List.of();
        return heroes.stream()
            .filter(Hero::isHomeVillage)
            .toList();
    }
    
    /**
     * Filter troops to home village only, excluding pets.
     * Pets are identified by known pet names.
     */
    public List<LeveledItem> homeTroops() {
        if (troops == null) return List.of();
        return troops.stream()
            .filter(LeveledItem::isHomeVillage)
            .filter(t -> !isPet(t.name()))
            .toList();
    }
    
    /**
     * Extract pets from troops array (home village only).
     */
    public List<LeveledItem> pets() {
        if (troops == null) return List.of();
        return troops.stream()
            .filter(LeveledItem::isHomeVillage)
            .filter(t -> isPet(t.name()))
            .toList();
    }
    
    /**
     * Filter spells to home village only.
     */
    public List<LeveledItem> homeSpells() {
        if (spells == null) return List.of();
        return spells.stream()
            .filter(LeveledItem::isHomeVillage)
            .toList();
    }
    
    private static boolean isPet(String name) {
        return PET_NAMES.contains(name);
    }
    
    /**
     * Known pet names as of TH17.
     * Update this list when new pets are added.
     */
    private static final java.util.Set<String> PET_NAMES = java.util.Set.of(
        "L.A.S.S.I",
        "Mighty Yak",
        "Electro Owl",
        "Unicorn",
        "Phoenix",
        "Poison Lizard",
        "Diggy",
        "Frosty",
        "Spirit Fox",
        "Angry Jelly",
        "Sneezy"
    );
}
```

### Clan Endpoint Response

**Endpoint:** `GET /clans/{clanTag}`

```java
package com.coctracker.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Clan member in the member list.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClanMember(
    String tag,
    String name,
    String role,                // "member", "elder", "coLeader", "leader"
    Integer expLevel,
    Integer trophies,
    Integer donations,
    Integer donationsReceived,
    Integer clanRank,           // Position in clan (1 = highest trophies)
    Integer previousClanRank
) {}

/**
 * War league info for the clan.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WarLeague(
    Integer id,
    String name                 // e.g., "Champion League I"
) {}

/**
 * Full clan response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClanResponse(
    String tag,
    String name,
    Integer clanLevel,
    Integer clanPoints,
    Integer warWins,
    Integer warTies,
    Integer warLosses,
    Integer members,            // Count of members
    String description,
    boolean isWarLogPublic,
    WarLeague warLeague,
    BadgeUrls badgeUrls,
    List<ClanMember> memberList
) {}
```

### CWL League Group Response

**Endpoint:** `GET /clans/{clanTag}/currentwar/leaguegroup`

```java
package com.coctracker.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Member info within CWL group (minimal - just tag, name, TH).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueGroupMember(
    String tag,
    String name,
    Integer townHallLevel
) {}

/**
 * Clan info within CWL group.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueGroupClan(
    String tag,
    String name,
    Integer clanLevel,
    BadgeUrls badgeUrls,
    List<LeagueGroupMember> members  // CWL roster (up to 35)
) {}

/**
 * A single round containing war tags.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueRound(
    List<String> warTags        // 4 war tags per round (8 clans = 4 matchups)
) {}

/**
 * CWL league group response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueGroupResponse(
    String state,               // "preparation", "inWar", "ended"
    String season,              // "2025-02" format
    List<LeagueGroupClan> clans,
    List<LeagueRound> rounds    // 7 rounds
) {
    
    /**
     * Find our clan in the group by tag.
     */
    public LeagueGroupClan findClan(String clanTag) {
        if (clans == null) return null;
        return clans.stream()
            .filter(c -> c.tag().equals(clanTag))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get war tags for a specific round (1-7).
     */
    public List<String> getWarTagsForRound(int roundNumber) {
        if (rounds == null || roundNumber < 1 || roundNumber > rounds.size()) {
            return List.of();
        }
        return rounds.get(roundNumber - 1).warTags();
    }
    
    /**
     * Find which war tag involves our clan for a given round.
     * Returns null if not determinable from this response alone.
     * (Requires fetching each war to check participants)
     */
    public String findOurWarTag(int roundNumber, String ourClanTag) {
        // Note: League group doesn't tell us which war tag is ours.
        // We must fetch each war and check clan.tag or opponent.tag.
        // This method is a placeholder - actual logic in service layer.
        return null;
    }
}
```

### CWL War Response

**Endpoint:** `GET /clanwarleagues/wars/{warTag}`

```java
package com.coctracker.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Individual attack in a war.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WarAttack(
    String attackerTag,
    String defenderTag,
    Integer stars,
    Integer destructionPercentage,
    Integer order               // Attack order in the war (1-based)
) {}

/**
 * War member with position and attacks.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WarMember(
    String tag,
    String name,
    Integer townhallLevel,      // Note: lowercase 'h' in API response
    Integer mapPosition,        // 1-15
    List<WarAttack> attacks,    // Empty/null if no attack yet
    Integer opponentAttacks     // Number of times this base was attacked
) {
    
    /**
     * Check if this member has used their attack.
     */
    public boolean hasAttacked() {
        return attacks != null && !attacks.isEmpty();
    }
    
    /**
     * Get the single attack (CWL = 1 attack per member).
     */
    public WarAttack getAttack() {
        if (attacks == null || attacks.isEmpty()) return null;
        return attacks.get(0);
    }
}

/**
 * Clan info within a war.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WarClan(
    String tag,
    String name,
    Integer clanLevel,
    Integer attacks,            // Total attacks used
    Integer stars,              // Total stars earned
    Double destructionPercentage,
    BadgeUrls badgeUrls,
    List<WarMember> members
) {
    
    /**
     * Find a member by tag.
     */
    public WarMember findMember(String playerTag) {
        if (members == null) return null;
        return members.stream()
            .filter(m -> m.tag().equals(playerTag))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Find a member by map position.
     */
    public WarMember findMemberByPosition(int position) {
        if (members == null) return null;
        return members.stream()
            .filter(m -> m.mapPosition() == position)
            .findFirst()
            .orElse(null);
    }
}

/**
 * CWL war response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CwlWarResponse(
    String warTag,              // Only present for CWL wars (not regular wars)
    String state,               // "preparation", "inWar", "warEnded"
    Integer teamSize,           // 15 for CWL
    Integer attacksPerMember,   // 1 for CWL (may be null — use Integer!)
    String preparationStartTime,
    String startTime,
    String endTime,
    WarClan clan,
    WarClan opponent
) {
    
    /**
     * Check if war is currently active (attacks can happen).
     */
    public boolean isInWar() {
        return "inWar".equals(state);
    }
    
    /**
     * Check if war has ended.
     */
    public boolean hasEnded() {
        return "warEnded".equals(state);
    }
    
    /**
     * Check if still in preparation phase.
     */
    public boolean isPreparation() {
        return "preparation".equals(state);
    }
    
    /**
     * Determine war result for our clan.
     * Returns "WIN", "LOSE", or "TIE".
     */
    public String determineResult() {
        if (!hasEnded()) return null;
        
        int ourStars = clan.stars();
        int theirStars = opponent.stars();
        
        if (ourStars > theirStars) return "WIN";
        if (ourStars < theirStars) return "LOSE";
        
        // Stars tied - check destruction
        double ourDestruction = clan.destructionPercentage();
        double theirDestruction = opponent.destructionPercentage();
        
        if (ourDestruction > theirDestruction) return "WIN";
        if (ourDestruction < theirDestruction) return "LOSE";
        
        return "TIE";
    }
    
    /**
     * Check if the given clan tag is "our" clan (vs opponent).
     */
    public boolean isOurClan(String clanTag) {
        return clan != null && clan.tag().equals(clanTag);
    }
    
    /**
     * Get our clan's data, given our clan tag.
     */
    public WarClan getOurClan(String ourClanTag) {
        if (clan != null && clan.tag().equals(ourClanTag)) return clan;
        if (opponent != null && opponent.tag().equals(ourClanTag)) return opponent;
        return null;
    }
    
    /**
     * Get opponent clan's data, given our clan tag.
     */
    public WarClan getOpponentClan(String ourClanTag) {
        if (clan != null && clan.tag().equals(ourClanTag)) return opponent;
        if (opponent != null && opponent.tag().equals(ourClanTag)) return clan;
        return null;
    }
}
```

---

## API to Database Mapping

### Player Response → Database Tables

| API Field | Database Table | Database Column |
|-----------|----------------|-----------------|
| `tag` | `player` | `tag` (PK) |
| `name` | `player` | `name` |
| `townHallLevel` | `player` | `town_hall_level` |
| `clan.tag` | `player` | `clan_tag` (FK) |
| `role` | `player` | `clan_role` |
| `warStars` | `player` | `war_stars` |
| `donations` | `player` | `donations` |
| `donationsReceived` | `player` | `donations_received` |
| `heroes[]` (filtered) | `player_hero` | One row per hero |
| `heroes[].equipment[]` | `player_equipment` | One row per equipped item |
| `troops[]` (filtered, no pets) | `player_troop` | One row per troop |
| `spells[]` (filtered) | `player_spell` | One row per spell |
| `troops[]` (pets only) | `player_pet` | One row per pet |

### Clan Response → Database Tables

| API Field | Database Table | Database Column |
|-----------|----------------|-----------------|
| `tag` | `clan` | `tag` (PK) |
| `name` | `clan` | `name` |
| `clanLevel` | `clan` | `clan_level` |
| `clanPoints` | `clan` | `clan_points` |
| `warWins` | `clan` | `war_wins` |
| `warTies` | `clan` | `war_ties` |
| `warLosses` | `clan` | `war_losses` |
| `description` | `clan` | `description` |
| `memberList[]` | `player` | Updates existing players |

### League Group Response → Database Tables

| API Field | Database Table | Database Column |
|-----------|----------------|-----------------|
| `season` | `cwl_season` | `season` |
| League name (from clan) | `cwl_season` | `league_name` |
| `clans[ourClan].members[]` | `cwl_participant` | One row per roster member |
| Player snapshot | `cwl_participant` | `stats_snapshot` (JSONB) |

### CWL War Response → Database Tables

| API Field | Database Table | Database Column |
|-----------|----------------|-----------------|
| `warTag` | `cwl_war` | `war_tag` |
| Round number (derived) | `cwl_war` | `day_number` |
| `opponent.tag` | `cwl_war` | `opponent_clan_tag` |
| `opponent.name` | `cwl_war` | `opponent_clan_name` |
| `clan.stars` | `cwl_war` | `our_stars` |
| `clan.destructionPercentage` | `cwl_war` | `our_destruction` |
| `opponent.stars` | `cwl_war` | `opponent_stars` |
| `opponent.destructionPercentage` | `cwl_war` | `opponent_destruction` |
| Result (derived) | `cwl_war` | `result` |
| `clan.members[]` | `cwl_war_member` | One row per participant |
| `member.mapPosition` | `cwl_war_member` | `map_position` |
| `member.attacks[0]` | `cwl_attack` | Attack details |

### Attack Mapping Details

| API Field (WarAttack) | Database Column (cwl_attack) |
|-----------------------|------------------------------|
| `attackerTag` | `attacker_tag` |
| Derived from war | `attacker_map_position` |
| `defenderTag` | `defender_tag` |
| Derived from opponent | `defender_name` |
| Opponent member TH | `defender_th_level` |
| Opponent member position | `defender_map_position` |
| `stars` | `stars` |
| `destructionPercentage` | `destruction_percentage` |
| `order` | `attack_order` |

---

## Data Filtering Rules

### Village Filter
Only store `village: "home"` items. Exclude:
- Builder Base heroes (Battle Machine, Battle Copter)
- Builder Base troops (Raged Barbarian, Sneaky Archer, etc.)
- Builder Base spells (if any)

### Pet Identification
Pets are embedded in the `troops` array. Filter by known names:
```java
Set<String> PET_NAMES = Set.of(
    "L.A.S.S.I", "Mighty Yak", "Electro Owl", "Unicorn",
    "Phoenix", "Poison Lizard", "Diggy", "Frosty",
    "Spirit Fox", "Angry Jelly", "Sneezy"
);
```

### Equipment Source
Use **nested** `heroes[].equipment[]` (what's equipped), NOT the flat `heroEquipment[]` (what's owned).

### Super Troops
Include in troops list. Note `superTroopIsActive: true` flag for currently boosted super troops.

---

## Error Handling

### CoC API Status Codes

| Status | Meaning | Action |
|--------|---------|--------|
| 200 | Success | Process response |
| 400 | Bad request (invalid tag format) | Log error, skip |
| 403 | Forbidden (invalid API key or IP) | Alert, check configuration |
| 404 | Not found (player/clan doesn't exist) | Mark as inactive or remove |
| 429 | Rate limited | Back off, retry with exponential delay |
| 500+ | Server error | Retry with backoff |
| 503 | Maintenance | Pause polling, retry later |

### GlobalExceptionHandler Status Mapping

The `GlobalExceptionHandler` maps CoC API error codes to proper HTTP responses for our API consumers:

```java
HttpStatus httpStatus = switch (ex.getStatusCode()) {
    case 404 -> HttpStatus.NOT_FOUND;       // Clan/player doesn't exist
    case 403 -> HttpStatus.FORBIDDEN;       // Bad API token
    case 429 -> HttpStatus.TOO_MANY_REQUESTS; // Rate limited
    case 400 -> HttpStatus.BAD_REQUEST;     // Invalid tag format
    default  -> HttpStatus.BAD_GATEWAY;     // CoC API is down
};
```

This ensures our frontend receives meaningful status codes instead of always getting 502 Bad Gateway.

### Common Error Response

```json
{
  "reason": "notFound",
  "message": "Player not found"
}
```

### Jackson 3.x Note: Use Integer, Not int

**All numeric fields in API DTOs MUST use `Integer`/`Double` (boxed types), not `int`/`double`.**

Jackson 3.x enables `FAIL_ON_NULL_FOR_PRIMITIVES` by default. If the CoC API omits a field (e.g., `attacksPerMember` is sometimes absent), deserialization fails with:
```
Cannot map null into type int (set FAIL_ON_NULL_FOR_PRIMITIVES to 'false')
```

When narrowing `Integer` to `short` for database storage, use the double-cast pattern:
```java
(short)(int) apiResponse.townhallLevel()  // unbox Integer→int, then narrow int→short
```

### Private War Log

If `isWarLogPublic: false` on clan response:
- Cannot fetch war details
- Inform user and skip tracking for that clan

---

## Stats Snapshot JSON Structure

Stored in `cwl_participant.stats_snapshot` as JSONB:

```json
{
  "townHallLevel": 17,
  "heroes": [
    {
      "name": "Barbarian King",
      "level": 52,
      "maxLevel": 105,
      "equipment": [
        { "name": "Spiky Ball", "level": 27, "maxLevel": 27 },
        { "name": "Snake Bracelet", "level": 20, "maxLevel": 27 }
      ]
    },
    {
      "name": "Archer Queen",
      "level": 54,
      "maxLevel": 105,
      "equipment": [
        { "name": "Action Figure", "level": 20, "maxLevel": 27 },
        { "name": "Magic Mirror", "level": 20, "maxLevel": 27 }
      ]
    }
  ],
  "pets": [
    { "name": "L.A.S.S.I", "level": 4, "maxLevel": 15 },
    { "name": "Mighty Yak", "level": 6, "maxLevel": 15 }
  ],
  "troops": [
    { "name": "Barbarian", "level": 12, "maxLevel": 12 },
    { "name": "Archer", "level": 8, "maxLevel": 13 }
  ],
  "spells": [
    { "name": "Lightning Spell", "level": 10, "maxLevel": 13 },
    { "name": "Healing Spell", "level": 10, "maxLevel": 12 }
  ],
  "snapshotTakenAt": "2025-02-01T15:30:00Z"
}
```

---

## Rate Limiting Considerations

- **Limit:** ~30-40 requests/second (undocumented, observed)
- **Strategy:** Use semaphore or token bucket to throttle requests
- **Batch fetching:** When syncing multiple players, spread requests over time
- **Priority:** CWL war polling takes priority during active wars

### Suggested Intervals

| Scenario | Interval |
|----------|----------|
| Active CWL war | 2-5 minutes |
| CWL prep day | 30 minutes |
| No active war | 2-4 hours |
| Initial clan sync | Stagger player fetches over 30 seconds |

---

## Notes for Implementation

1. **War tag discovery:** League group response gives 4 war tags per round, but doesn't say which one is ours. Must fetch each and check `clan.tag` or `opponent.tag`.

2. **Attack deduplication:** Use `(cwl_war_id, attacker_tag)` unique constraint to prevent duplicate attack records.

3. **Snapshot timing:** Capture player snapshot on their first attack of the season, not at season start (player might upgrade during CWL).

4. **Position changes:** Player map positions can differ each day. Store position per war, not per season.

5. **Incomplete wars:** If war tag returns `state: "preparation"`, attacks aren't available yet. Skip and retry later.
