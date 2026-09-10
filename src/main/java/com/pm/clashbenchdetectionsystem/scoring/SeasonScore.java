package com.pm.clashbenchdetectionsystem.scoring;

import java.util.List;

public record SeasonScore(
        String playerTag,
        String playerName,
        int townHallLevel,
        int totalScore,
        int attacksMade,
        int attacksMissed,
        double averageStars,
        double averageDestruction,
        List<ScoredAttack> scoredAttacks
) {}
