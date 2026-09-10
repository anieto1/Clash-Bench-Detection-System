# Clash Bench Detection System (CBDS)

## CWL Performance Scoring Algorithm

### Overview

The **Clash Bench Detection System** calculates a performance score for each attack in Clan War League (CWL). Scores are calculated **per attack**, then summed for a cumulative season score.

The system answers one question: **Who's carrying their weight, and who should be on the bench?**

**Scoring philosophy:**
- **Participation is mandatory** — Missing an attack is the worst offense
- **3-stars are the goal** — Anything less requires context to evaluate
- **Context matters** — Who you attacked changes expectations
- **Risk is rewarded** — Attacking top bases earns a bonus
- **Bottom bases are gimmes** — Positions 13-15 must be 3-starred

---

## Scoring Components

### 1. PARTICIPATION CHECK

```
If player did NOT attack:
    score = -100
    STOP (no further calculation)
```

Missing an attack is catastrophic for the clan. This penalty ensures any non-participant 
ranks below everyone who showed up, regardless of their performance.

---

### 2. BASE SCORE (Stars)

| Stars | Points |
|-------|--------|
| 3     | 100    |
| 2     | 50     |
| 1     | 10     |
| 0     | 0      |

---

### 3. DESTRUCTION MODIFIER

Only applies when stars < 3. Destruction percentage determines how close you were to success.

**For 2-star attacks:**

| Destruction % | Modifier | Reasoning |
|---------------|----------|-----------|
| 95-99%        | +15      | Time fail, unlucky |
| 87-94%        | +10      | Close attempt |
| 70-86%        | +0       | Mediocre |
| 50-69%        | -10      | Poor execution |
| < 50%         | -20      | Bad attack |

**For 1-star attacks:**

| Destruction % | Modifier | Reasoning |
|---------------|----------|-----------|
| 70%+          | +0       | At least made progress |
| 50-69%        | -10      | Poor |
| < 50%         | -20      | Very poor |

**For 0-star attacks:**

| Destruction % | Modifier | Reasoning |
|---------------|----------|-----------|
| 50%+          | -10      | Some effort shown |
| < 50%         | -25      | Disaster |

---

### 4. DIFFICULTY MODIFIER

Two dimensions determine attack difficulty:

#### A. Position Differential (Map Position)

```
position_diff = attacker_map_position - defender_map_position
```

- **Negative value** = hitting UP (attacking a harder base)
- **Positive value** = hitting DOWN (attacking an easier base)

| Position Diff | Modifier | Description |
|---------------|----------|-------------|
| -5 or lower   | +20      | Heroic (hitting way up) |
| -4 to -3      | +10      | Ambitious |
| -2 to +2      | +0       | Expected matchup |
| +3 to +4      | -10      | Playing it safe |
| +5 or higher  | -15      | Easy target |

#### B. Town Hall Differential

```
th_diff = attacker_TH - defender_TH
```

| TH Diff    | Modifier | Description |
|------------|----------|-------------|
| Negative   | +10      | Attacked stronger TH |
| Zero       | +0       | Same TH level |
| Positive   | -10      | Attacked weaker TH |

---

### 5. TOP BASE BONUS

Attacking the top 3 bases (positions 1, 2, 3) is inherently risky. 
Regardless of outcome, you earn a bonus for taking on the hardest targets.

```
If defender_map_position <= 3:
    top_base_bonus = +15
```

This rewards players who step up to the challenge. A 3-star on a top base 
becomes exceptional; a failed attempt still acknowledges the difficulty.

---

### 6. GIMME PENALTY

Bottom 3 bases (positions 13, 14, 15) are expected to be 3-starred. 
Failing to do so incurs an additional penalty.

```
If defender_map_position >= 13 AND stars < 3:
    gimme_penalty = -20
```

---

## Complete Formula

```python
def calculate_attack_score(attack, attacker, defender):
    """
    Calculate CBDS score for a single CWL attack.
    
    Returns: Integer score (typically -100 to +145 range)
    """
    
    # Check participation
    if attack is None:
        return -100
    
    stars = attack.stars
    destruction = attack.destruction_percentage
    attacker_pos = attack.attacker_map_position
    defender_pos = attack.defender_map_position
    attacker_th = attacker.town_hall_level
    defender_th = defender.town_hall_level
    
    # 1. Base score from stars
    base_scores = {3: 100, 2: 50, 1: 10, 0: 0}
    score = base_scores[stars]
    
    # 2. Destruction modifier (only if not 3-star)
    destruction_mod = 0
    if stars < 3:
        destruction_mod = get_destruction_modifier(stars, destruction)
    
    # 3. Position difficulty modifier
    position_diff = attacker_pos - defender_pos
    position_mod = get_position_modifier(position_diff)
    
    # 4. Town Hall difficulty modifier
    th_diff = attacker_th - defender_th
    if th_diff < 0:
        th_mod = +10   # Attacked higher TH
    elif th_diff > 0:
        th_mod = -10   # Attacked weaker TH
    else:
        th_mod = 0     # Same TH
    
    # 5. Top base bonus (positions 1-3)
    top_base_bonus = 0
    if defender_pos <= 3:
        top_base_bonus = +15
    
    # 6. Gimme penalty (positions 13-15 must be 3-starred)
    gimme_penalty = 0
    if defender_pos >= 13 and stars < 3:
        gimme_penalty = -20
    
    # Calculate total
    total = score + destruction_mod + position_mod + th_mod + top_base_bonus + gimme_penalty
    
    return total


def get_destruction_modifier(stars, destruction):
    """Get destruction-based modifier for non-3-star attacks."""
    
    if stars == 2:
        if destruction >= 95: return +15
        if destruction >= 87: return +10
        if destruction >= 70: return 0
        if destruction >= 50: return -10
        return -20
    
    elif stars == 1:
        if destruction >= 70: return 0
        if destruction >= 50: return -10
        return -20
    
    else:  # 0 stars
        if destruction >= 50: return -10
        return -25


def get_position_modifier(position_diff):
    """Get modifier based on attacker vs defender map position."""
    
    if position_diff <= -5: return +20   # Hit way up
    if position_diff <= -3: return +10   # Hit up
    if position_diff <= 2:  return 0     # Even matchup
    if position_diff <= 4:  return -10   # Hit down
    return -15                            # Hit way down


def calculate_season_score(player_attacks):
    """
    Calculate total CBDS score for a CWL season.
    
    Args:
        player_attacks: List of 7 attacks (None for missed attacks)
    
    Returns: Integer season score
    """
    return sum(calculate_attack_score(attack) for attack in player_attacks)
```

---

## Score Ranges

### Per-Attack Scores

| Score Range | Performance Level | Description |
|-------------|-------------------|-------------|
| 130 - 145   | Exceptional       | 3-star on a top base while hitting up |
| 110 - 129   | Excellent         | 3-star hitting up, or perfect top base |
| 100 - 109   | Great             | 3-star on expected target |
| 85 - 99     | Good              | 3-star hitting down, or solid 2-star up |
| 50 - 84     | Average           | 2-star with reasonable context |
| 20 - 49     | Below Average     | Weak 2-star or context issues |
| 0 - 19      | Poor              | 1-star or failed expectations |
| -25 - -1    | Bad               | 0-star or major failures |
| -100        | Absent            | Did not participate |

### Season Scores (7 attacks)

| Score Range | Performance Level |
|-------------|-------------------|
| 800+        | Elite             |
| 650 - 799   | Strong            |
| 500 - 649   | Solid             |
| 350 - 499   | Average           |
| 200 - 349   | Struggling        |
| 0 - 199     | Poor              |
| Below 0     | Bench Material    |

---

## Example Calculations

### Example 1: Exceptional — Heroic 3-star on top base
**#10 attacks #2, gets 3 stars 100%**
```
Base score:        100 (3 stars)
Destruction mod:   +0  (N/A for 3-star)
Position mod:      +20 (hit up 8 positions)
TH mod:            +10 (likely higher TH)
Top base bonus:    +15 (position 2)
Gimme penalty:     +0  (N/A)
─────────────────────────
TOTAL:             145
```

### Example 2: Excellent — 3-star hitting up
**#10 attacks #5, gets 3 stars 100%**
```
Base score:        100 (3 stars)
Position mod:      +20 (hit up 5 positions)
TH mod:            +10 (likely higher TH)
─────────────────────────
TOTAL:             130
```

### Example 3: Great — Expected 3-star
**#8 attacks #8, gets 3 stars 100%**
```
Base score:        100 (3 stars)
Position mod:      +0  (even matchup)
TH mod:            +0  (same TH)
─────────────────────────
TOTAL:             100
```

### Example 4: Good — Safe 3-star
**#5 attacks #13, gets 3 stars 100%**
```
Base score:        100 (3 stars)
Position mod:      -15 (hit down 8 positions)
TH mod:            -10 (likely lower TH)
─────────────────────────
TOTAL:             75
```

### Example 5: Excellent — Time fail on top base
**#8 attacks #2, gets 2 stars 97%**
```
Base score:        50  (2 stars)
Destruction mod:   +15 (time fail)
Position mod:      +20 (hit up 6 positions)
TH mod:            +10 (higher TH)
Top base bonus:    +15 (position 2)
─────────────────────────
TOTAL:             110
```

### Example 6: Average — Decent 2-star
**#8 attacks #6, gets 2 stars 88%**
```
Base score:        50  (2 stars)
Destruction mod:   +10 (close)
Position mod:      +0  (hit up 2, within range)
TH mod:            +0  (same TH)
─────────────────────────
TOTAL:             60
```

### Example 7: Below Average — Failed gimme
**#6 attacks #14, gets 2 stars 75%**
```
Base score:        50  (2 stars)
Destruction mod:   +0  (70-86% range)
Position mod:      -15 (hit down 8 positions)
TH mod:            -10 (lower TH)
Gimme penalty:     -20 (position 14, not 3-star)
─────────────────────────
TOTAL:             5
```

### Example 8: Bad — Disaster on easy base
**#3 attacks #15, gets 1 star 45%**
```
Base score:        10  (1 star)
Destruction mod:   -20 (below 50%)
Position mod:      -15 (hit down 12 positions)
TH mod:            -10 (lower TH)
Gimme penalty:     -20 (position 15, not 3-star)
─────────────────────────
TOTAL:             -55
```

### Example 9: Absent — No-show
**Player did not attack**
```
TOTAL:             -100
```

---

## Ranking Tiers

Final tier names TBD. Current working structure:

| Tier | Season Score | Name (Working) |
|------|--------------|----------------|
| S    | 800+         | ??? |
| A    | 650-799      | ??? |
| B    | 500-649      | ??? |
| C    | 350-499      | ??? |
| D    | 200-349      | ??? |
| F    | Below 200    | The Bench |

---

## Database Implementation

The scoring can be implemented as a PostgreSQL function or calculated in the application layer.

### Option A: Application Layer (Recommended for MVP)
Calculate scores in Java when displaying rankings. Simpler to adjust and test.

### Option B: Database Function
```sql
CREATE OR REPLACE FUNCTION calculate_cbds_score(
    p_stars SMALLINT,
    p_destruction DECIMAL(5,2),
    p_attacker_pos SMALLINT,
    p_defender_pos SMALLINT,
    p_attacker_th SMALLINT,
    p_defender_th SMALLINT,
    p_participated BOOLEAN
) RETURNS INTEGER AS $$
DECLARE
    score INTEGER := 0;
    destruction_mod INTEGER := 0;
    position_mod INTEGER := 0;
    th_mod INTEGER := 0;
    top_base_bonus INTEGER := 0;
    gimme_penalty INTEGER := 0;
    position_diff INTEGER;
    th_diff INTEGER;
BEGIN
    -- Participation check
    IF NOT p_participated THEN
        RETURN -100;
    END IF;
    
    -- Base score
    score := CASE p_stars
        WHEN 3 THEN 100
        WHEN 2 THEN 50
        WHEN 1 THEN 10
        ELSE 0
    END;
    
    -- Destruction modifier (non-3-star only)
    IF p_stars < 3 THEN
        IF p_stars = 2 THEN
            destruction_mod := CASE
                WHEN p_destruction >= 95 THEN 15
                WHEN p_destruction >= 87 THEN 10
                WHEN p_destruction >= 70 THEN 0
                WHEN p_destruction >= 50 THEN -10
                ELSE -20
            END;
        ELSIF p_stars = 1 THEN
            destruction_mod := CASE
                WHEN p_destruction >= 70 THEN 0
                WHEN p_destruction >= 50 THEN -10
                ELSE -20
            END;
        ELSE
            destruction_mod := CASE
                WHEN p_destruction >= 50 THEN -10
                ELSE -25
            END;
        END IF;
    END IF;
    
    -- Position modifier
    position_diff := p_attacker_pos - p_defender_pos;
    position_mod := CASE
        WHEN position_diff <= -5 THEN 20
        WHEN position_diff <= -3 THEN 10
        WHEN position_diff <= 2 THEN 0
        WHEN position_diff <= 4 THEN -10
        ELSE -15
    END;
    
    -- TH modifier
    th_diff := p_attacker_th - p_defender_th;
    th_mod := CASE
        WHEN th_diff < 0 THEN 10
        WHEN th_diff > 0 THEN -10
        ELSE 0
    END;
    
    -- Top base bonus
    IF p_defender_pos <= 3 THEN
        top_base_bonus := 15;
    END IF;
    
    -- Gimme penalty
    IF p_defender_pos >= 13 AND p_stars < 3 THEN
        gimme_penalty := -20;
    END IF;
    
    RETURN score + destruction_mod + position_mod + th_mod + top_base_bonus + gimme_penalty;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
```

---

## Future Considerations

1. **Weighted recency**: More recent seasons could count more than older ones
2. **Consistency bonus**: Reward players who always show up across multiple seasons
3. **Improvement tracking**: Track if players are getting better or worse over time
4. **Head-to-head comparison**: Compare two players directly
5. **Clan-level CBDS**: Aggregate score to evaluate clan strength
6. **Adjustable weights**: Allow clan leaders to tweak the formula for their priorities
