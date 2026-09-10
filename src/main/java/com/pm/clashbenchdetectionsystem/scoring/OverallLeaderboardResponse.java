package com.pm.clashbenchdetectionsystem.scoring;

import java.math.BigDecimal;
import java.util.List;

public record OverallLeaderboardResponse(
        List<OverallEntry> entries
) {
    public record OverallEntry(
            int rank,
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
}
