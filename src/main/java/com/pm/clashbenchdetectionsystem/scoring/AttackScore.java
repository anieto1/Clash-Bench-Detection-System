package com.pm.clashbenchdetectionsystem.scoring;

public record AttackScore(
        int baseScore,
        int destructionModifier,
        int positionModifier,
        int thModifier,
        int topBaseBonus,
        int gimmePenalty,
        int totalScore
) {
    public static final AttackScore ABSENT = new AttackScore(0, 0, 0, 0, 0, 0, -100);
}
