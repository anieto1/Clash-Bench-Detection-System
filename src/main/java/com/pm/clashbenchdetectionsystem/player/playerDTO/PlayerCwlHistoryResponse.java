package com.pm.clashbenchdetectionsystem.player.playerDTO;

import java.math.BigDecimal;
import java.util.List;

public record PlayerCwlHistoryResponse(
        String playerTag,
        String playerName,
        List<SeasonEntry> seasons
) {
    public record SeasonEntry(
            String clanTag,
            String season,
            int totalScore,
            int attacksMade,
            int attacksMissed,
            BigDecimal averageStars,
            BigDecimal averageDestruction,
            int townHallLevel
    ) {}
}
