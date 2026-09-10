package com.pm.clashbenchdetectionsystem.cwl.cwlDTO;

import java.util.List;

public record LeaderBoardResponse(
        String season,
        String clanTag,
        List<LeaderBoardEntry> entries
) {
    public record LeaderBoardEntry(
            String playerTag,
            String playerName,
            int townHallLevel,
            int totalScore,
            int attacksMade,
            int attacksMissed,
            double averageStars,
            double averageDestruction,
            List<AttackScoreDto> attackScores
    ) {}

    public record AttackScoreDto(
            int attackerMapPosition,
            int defenderMapPosition,
            int attackerTh,
            int defenderTh,
            int stars,
            double destructionPercentage,
            int baseScore,
            int destructionModifier,
            int positionModifier,
            int thModifier,
            int topBaseBonus,
            int gimmePenalty,
            int totalScore
    ) {}
}
