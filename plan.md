# Plan: Add attacker TH level to cwl_war_member

## Problem
The scoring algorithm needs the attacker's TH level per war day, but currently reads it from `cwl_participant.stats_snapshot` (a one-time season snapshot). The CoC API already provides `townhallLevel` on every war member — we just aren't storing it.

## Changes

### 1. V3 Migration — Add `town_hall_level` column to `cwl_war_member`
- `ALTER TABLE cwl_war_member ADD COLUMN town_hall_level SMALLINT;`
- Nullable since existing rows won't have it
- Add CHECK constraint `BETWEEN 1 AND 20`

### 2. `CwlWarMember.java` entity — Add `townHallLevel` field
- New `short townHallLevel` field with column mapping

### 3. `CwlService.syncWarMember()` — Capture TH from API response
- The `CocCwlWarResponse.WarMember` already has `townhallLevel`
- Pass it into the `CwlWarMember` constructor/setter during sync

### 4. `CbdsScoreService.calculateLeaderboard()` — Read TH from war member instead of snapshot
- Currently: `extractTownHallLevel(participant.getStatsSnapshot())`
- Change to: read from `CwlWarMember.townHallLevel` per attack
- Fall back to snapshot if war member TH is null (backward compat with old data)

### 5. `CwlMapper` — Include TH in war member DTOs (if needed for frontend)

## Files touched
- `src/main/resources/db/migration/V3__add_war_member_th_level.sql` (new)
- `src/main/java/.../cwl/CwlWarMember.java`
- `src/main/java/.../cwl/CwlService.java`
- `src/main/java/.../scoring/CbdsScoreService.java`
- `src/main/java/.../cwl/CwlMapper.java` (if needed)
