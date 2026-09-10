package com.pm.clashbenchdetectionsystem.cwl.cwlDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CwlSeasonResponse(
        String clanTag,
        String season,
        String leagueName,
        Short finalPlacement,
        int totalStars,
        BigDecimal totalDestruction,
        boolean completed,
        List<ParticipantDto> participants,
        List<WarSummaryDto> wars,
        Instant updatedAt
) {
    public record ParticipantDto(
            String playerTag,
            String playerName,
            int townHallLevel
    ) {}

    public record WarSummaryDto(
            String warTag,
            int dayNumber,
            String opponentClanName,
            int ourStars,
            int opponentStars,
            String result
    ) {}
}
