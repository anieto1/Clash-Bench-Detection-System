package com.pm.clashbenchdetectionsystem.scoring;

/**
 * Pairs a pure {@link AttackScore} with the raw attack context from the database.
 * This keeps scoring logic (CbdsCalculator) separate from display concerns.
 */
public record ScoredAttack(
        AttackScore score,
        int attackerMapPosition,
        int defenderMapPosition,
        int attackerTh,
        int defenderTh,
        int stars,
        double destructionPercentage
) {
    public static final ScoredAttack ABSENT = new ScoredAttack(AttackScore.ABSENT, 0, 0, 0, 0, 0, 0);
}
