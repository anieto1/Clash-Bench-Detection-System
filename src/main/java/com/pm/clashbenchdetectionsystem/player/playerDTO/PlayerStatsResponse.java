package com.pm.clashbenchdetectionsystem.player.playerDTO;

import java.math.BigDecimal;

public record PlayerStatsResponse(
        String playerTag,
        String playerName,
        int currentTownHallLevel,
        int totalCareerScore,
        int totalAttacks,
        int totalMissed,
        BigDecimal careerAverageStars,
        BigDecimal careerAverageDestruction,
        int seasonsParticipated
) {}
